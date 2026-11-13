import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { SemesterRecord } from '../types';
import {
  ArrowLeft,
  Calendar,
  Award,
  BookOpen,
  CheckCircle2,
  AlertCircle,
  Loader2,
} from 'lucide-react';

export const SemesterDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [semester, setSemester] = useState<SemesterRecord | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      loadSemester(id);
    }
  }, [id]);

  const loadSemester = async (semId: string) => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getSemesterById(semId);
      setSemester(data);
    } catch (err: any) {
      console.error('Failed to load semester:', err);
      setError('Could not retrieve semester record.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="py-20 flex flex-col items-center justify-center">
        <Loader2 className="w-8 h-8 text-brand-600 animate-spin mb-3" />
        <p className="text-xs text-slate-400">Loading semester details...</p>
      </div>
    );
  }

  if (error || !semester) {
    return (
      <div className="max-w-md mx-auto my-12 text-center p-8 bg-white rounded-2xl border border-slate-200">
        <AlertCircle className="w-10 h-10 text-red-500 mx-auto mb-3" />
        <h3 className="text-base font-bold text-slate-800">Semester Record Not Found</h3>
        <p className="text-xs text-slate-500 mt-1 mb-4">{error || 'Requested record does not exist.'}</p>
        <Link
          to="/history"
          className="inline-flex items-center space-x-2 px-4 py-2 bg-brand-600 text-white rounded-xl text-xs font-semibold"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to History</span>
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fadeIn pb-12">
      {/* Top Navigation */}
      <div>
        <Link
          to="/history"
          className="inline-flex items-center space-x-2 text-xs font-semibold text-slate-500 hover:text-slate-800 transition-colors mb-2"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Semester History</span>
        </Link>
        <h1 className="text-2xl font-bold tracking-tight text-slate-800">
          Semester {semester.semesterNumber} Record
        </h1>
        <p className="text-xs text-slate-400">
          {semester.academicYear} • Register No: {semester.registerNumber || '—'}
        </p>
      </div>

      {/* Overview Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm">
          <p className="text-[11px] font-semibold text-slate-400 uppercase">Semester SGPA</p>
          <p className="text-3xl font-extrabold text-brand-700 mt-1">
            {semester.sgpa?.toFixed(2)}
          </p>
          <p className="text-xs text-slate-400 mt-1">Weighted grade-point average</p>
        </div>

        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm">
          <p className="text-[11px] font-semibold text-slate-400 uppercase">Cumulative CGPA</p>
          <p className="text-3xl font-extrabold text-slate-800 mt-1">
            {semester.cgpa ? semester.cgpa.toFixed(2) : '—'}
          </p>
          <p className="text-xs text-slate-400 mt-1">Overall performance up to this semester</p>
        </div>

        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm">
          <p className="text-[11px] font-semibold text-slate-400 uppercase">Credits Summary</p>
          <p className="text-3xl font-extrabold text-slate-800 mt-1">
            {semester.passedCredits} <span className="text-sm font-normal text-slate-400">/ {semester.totalCredits}</span>
          </p>
          <p className="text-xs text-slate-400 mt-1">
            {semester.arrearCount > 0 ? (
              <span className="text-amber-600 font-semibold">{semester.arrearCount} Arrear(s)</span>
            ) : (
              <span className="text-emerald-600 font-semibold">All Courses Passed</span>
            )}
          </p>
        </div>
      </div>

      {/* Subject details table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div className="p-4 sm:p-6 border-b border-slate-100">
          <h3 className="text-base font-bold text-slate-800">Subject Breakdown</h3>
          <p className="text-xs text-slate-400">
            Registered courses, grades and credits for Semester {semester.semesterNumber}
          </p>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200/80 text-[11px] font-semibold uppercase tracking-wider text-slate-400">
                <th className="py-3 px-4 w-12">#</th>
                <th className="py-3 px-4 w-32">Subject Code</th>
                <th className="py-3 px-4">Subject Name</th>
                <th className="py-3 px-4 text-center w-20">Grade</th>
                <th className="py-3 px-4 text-center w-24">Grade Pt</th>
                <th className="py-3 px-4 text-center w-24">Credits</th>
                <th className="py-3 px-4 text-center w-28">Weighted Pts</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {semester.subjects?.map((sub, idx) => {
                const gp = sub.gradePoint ?? 0;
                const weighted = (sub.credits * gp).toFixed(1);
                return (
                  <tr key={idx} className="hover:bg-slate-50/50 transition-colors">
                    <td className="py-3 px-4 font-mono text-slate-400 text-center">{idx + 1}</td>
                    <td className="py-3 px-4 font-mono font-semibold text-slate-800">{sub.subjectCode}</td>
                    <td className="py-3 px-4 font-medium text-slate-700">{sub.subjectName}</td>
                    <td className="py-3 px-4 text-center font-bold">
                      <span
                        className={`inline-block px-2 py-0.5 rounded text-[11px] ${
                          sub.grade === 'U' || sub.grade === 'RA'
                            ? 'bg-amber-100 text-amber-800'
                            : 'bg-blue-50 text-brand-700'
                        }`}
                      >
                        {sub.grade}
                      </span>
                    </td>
                    <td className="py-3 px-4 text-center font-mono text-slate-600">{gp}</td>
                    <td className="py-3 px-4 text-center font-semibold text-slate-800">{sub.credits}</td>
                    <td className="py-3 px-4 text-center font-mono font-bold text-slate-800">{weighted}</td>
                  </tr>
                );
              })}
            </tbody>
            <tfoot>
              <tr className="bg-slate-50/80 font-bold border-t border-slate-200">
                <td colSpan={5} className="py-3 px-4 text-right text-slate-600 uppercase text-[11px]">
                  Total Credits
                </td>
                <td className="py-3 px-4 text-center text-slate-800">{semester.totalCredits}</td>
                <td className="py-3 px-4 text-center text-brand-700 font-mono">
                  {((semester.sgpa || 0) * (semester.totalCredits || 0)).toFixed(1)}
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>
  );
};


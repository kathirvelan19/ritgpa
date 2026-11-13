import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import { SemesterRecord } from '../types';
import {
  History,
  Calendar,
  Award,
  ArrowRight,
  BookOpen,
  AlertCircle,
  Loader2,
  Upload,
} from 'lucide-react';

export const HistoryPage: React.FC = () => {
  const [semesters, setSemesters] = useState<SemesterRecord[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getSemesters();
      setSemesters(data);
    } catch (err: any) {
      console.error('Failed to load semesters:', err);
      setError('Unable to load semester history.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 animate-fadeIn pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-800">Semester History</h1>
          <p className="text-sm text-slate-500 mt-1">
            Complete record of your evaluated semesters and cumulative academic performance.
          </p>
        </div>
        <Link
          to="/upload"
          className="inline-flex items-center space-x-2 px-4 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-700 text-white font-semibold text-xs shadow-md shadow-brand-600/20 transition-all self-start sm:self-auto"
        >
          <Upload className="w-3.5 h-3.5" />
          <span>Upload Another Semester</span>
        </Link>
      </div>

      {loading ? (
        <div className="py-20 flex flex-col items-center justify-center">
          <Loader2 className="w-8 h-8 text-brand-600 animate-spin mb-3" />
          <p className="text-xs text-slate-400">Loading semester records...</p>
        </div>
      ) : error ? (
        <div className="p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center space-x-3 text-xs text-red-700">
          <AlertCircle className="w-4 h-4 flex-shrink-0" />
          <span>{error}</span>
        </div>
      ) : semesters.length === 0 ? (
        <div className="bg-white rounded-2xl border border-dashed border-slate-300 p-12 text-center">
          <div className="w-16 h-16 rounded-2xl bg-blue-50 text-brand-600 flex items-center justify-center mx-auto mb-4">
            <History className="w-8 h-8" />
          </div>
          <h3 className="text-base font-bold text-slate-800">No Semester Records Saved</h3>
          <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1 mb-6">
            Upload your semester result PDF to calculate and store your semester performance.
          </p>
          <Link
            to="/upload"
            className="inline-flex items-center space-x-2 px-5 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-700 text-white font-medium text-xs shadow-md shadow-brand-600/20 transition-all"
          >
            <Upload className="w-4 h-4" />
            <span>Upload Result PDF</span>
          </Link>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {semesters.map((sem) => (
            <div
              key={sem.id || sem.semesterNumber}
              className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm hover:shadow-md hover:border-brand-300 transition-all duration-200 flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-4">
                  <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-lg bg-blue-50 text-brand-700 font-bold text-xs border border-blue-100">
                    <span>Semester {sem.semesterNumber}</span>
                  </div>
                  <span
                    className={`text-[11px] font-bold px-2.5 py-0.5 rounded-full ${
                      sem.arrearCount > 0
                        ? 'bg-amber-100 text-amber-800'
                        : 'bg-emerald-100 text-emerald-800'
                    }`}
                  >
                    {sem.arrearCount > 0 ? `${sem.arrearCount} Arrears` : 'All Cleared'}
                  </span>
                </div>

                <div className="grid grid-cols-2 gap-4 py-3 border-y border-slate-100">
                  <div>
                    <p className="text-[11px] font-semibold text-slate-400 uppercase">SGPA</p>
                    <p className="text-2xl font-extrabold text-brand-700 mt-0.5">
                      {sem.sgpa?.toFixed(2)}
                    </p>
                  </div>
                  <div>
                    <p className="text-[11px] font-semibold text-slate-400 uppercase">Cumulative CGPA</p>
                    <p className="text-2xl font-extrabold text-slate-800 mt-0.5">
                      {sem.cgpa ? sem.cgpa.toFixed(2) : '—'}
                    </p>
                  </div>
                </div>

                <div className="mt-4 space-y-1 text-xs text-slate-500">
                  <p>
                    <span className="text-slate-400">Academic Year:</span>{' '}
                    <span className="font-medium text-slate-700">{sem.academicYear || '—'}</span>
                  </p>
                  <p>
                    <span className="text-slate-400">Total Credits:</span>{' '}
                    <span className="font-medium text-slate-700">
                      {sem.totalCredits} ({sem.passedCredits} passed)
                    </span>
                  </p>
                  <p>
                    <span className="text-slate-400">Subjects Count:</span>{' '}
                    <span className="font-medium text-slate-700">{sem.subjects?.length || 0}</span>
                  </p>
                </div>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-100 flex items-center justify-end">
                <Link
                  to={`/semesters/${sem.id || sem.semesterNumber}`}
                  className="inline-flex items-center space-x-1.5 text-xs font-semibold text-brand-600 hover:text-brand-700"
                >
                  <span>View Full Subject Details</span>
                  <ArrowRight className="w-3.5 h-3.5" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};


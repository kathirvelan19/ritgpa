import React, { useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { CalculateGpaResponse, SemesterRecord } from '../types';
import { api } from '../services/api';
import {
  Award,
  CheckCircle2,
  BookmarkCheck,
  ArrowRight,
  TrendingUp,
  BookOpen,
  Calendar,
  AlertCircle,
  Loader2,
  LayoutDashboard,
} from 'lucide-react';

export const ResultSummaryPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const stateData = location.state as {
    calculationResult?: CalculateGpaResponse;
    semesterInfo?: {
      studentName?: string;
      registerNumber?: string;
      academicYear?: string;
      semesterNumber?: string;
      course?: string;
    };
  };

  const calculationResult = stateData?.calculationResult;
  const semesterInfo = stateData?.semesterInfo;

  const [isSaving, setIsSaving] = useState<boolean>(false);
  const [saveSuccess, setSaveSuccess] = useState<boolean>(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  if (!calculationResult || !semesterInfo) {
    return (
      <div className="max-w-md mx-auto my-12 text-center p-8 bg-white rounded-2xl border border-slate-200">
        <AlertCircle className="w-10 h-10 text-amber-500 mx-auto mb-3" />
        <h3 className="text-base font-bold text-slate-800">No Calculation Result</h3>
        <p className="text-xs text-slate-500 mt-1 mb-4">
          Please upload a result PDF and enter credits to view this page.
        </p>
        <Link
          to="/upload"
          className="inline-flex items-center space-x-2 px-4 py-2 bg-brand-600 text-white rounded-xl text-xs font-semibold"
        >
          <span>Go to Upload</span>
        </Link>
      </div>
    );
  }

  const handleSaveSemester = async () => {
    setIsSaving(true);
    setSaveError(null);
    try {
      const recordPayload: SemesterRecord = {
        semesterNumber: semesterInfo.semesterNumber || '04',
        studentName: semesterInfo.studentName,
        registerNumber: semesterInfo.registerNumber,
        academicYear: semesterInfo.academicYear,
        course: semesterInfo.course,
        subjects: calculationResult.subjects,
        totalCredits: calculationResult.totalCredits,
        passedCredits: calculationResult.passedCredits,
        sgpa: calculationResult.sgpa,
        arrearCount: calculationResult.arrearCount,
        extractionStatus: 'VERIFIED_CALCULATED',
      };

      await api.saveSemester(recordPayload);
      setSaveSuccess(true);
    } catch (err: any) {
      console.error('Failed to save semester:', err);
      setSaveError(err?.response?.data?.message || err?.message || 'Failed to save record.');
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6 animate-fadeIn pb-12">
      {/* Success Notification */}
      {saveSuccess && (
        <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-2xl flex items-center justify-between text-xs text-emerald-800">
          <div className="flex items-center space-x-2.5">
            <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />
            <span className="font-medium">
              Semester {semesterInfo.semesterNumber} results successfully saved to your academic history!
            </span>
          </div>
          <Link
            to="/history"
            className="font-bold underline hover:text-emerald-900 ml-4 flex-shrink-0"
          >
            View History
          </Link>
        </div>
      )}

      {/* Save Error */}
      {saveError && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center space-x-3 text-xs text-red-700">
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span>{saveError}</span>
        </div>
      )}

      {/* Big GPA Highlight Card */}
      <div className="bg-gradient-to-br from-slate-900 via-blue-950 to-indigo-950 rounded-3xl p-8 sm:p-10 text-white shadow-xl shadow-blue-950/20 relative overflow-hidden">
        <div className="relative z-10 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div>
            <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-blue-500/20 text-sky-300 text-xs font-semibold mb-3 border border-blue-500/30">
              <Calendar className="w-3.5 h-3.5" />
              <span>Semester {semesterInfo.semesterNumber} • {semesterInfo.academicYear}</span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
              {semesterInfo.studentName || 'Student'}
            </h1>
            <p className="text-slate-300 text-xs mt-1">
              Register No: <span className="font-mono text-white">{semesterInfo.registerNumber}</span> • {semesterInfo.course}
            </p>
          </div>

          <div className="flex items-center space-x-4 bg-white/10 backdrop-blur px-6 py-4 rounded-2xl border border-white/10 self-stretch sm:self-auto justify-between sm:justify-start">
            <div>
              <p className="text-[11px] uppercase tracking-wider font-semibold text-sky-300">
                Calculated SGPA
              </p>
              <p className="text-4xl sm:text-5xl font-black tracking-tight text-white mt-1">
                {calculationResult.sgpa.toFixed(2)}
              </p>
            </div>
            <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-blue-500 to-sky-400 text-white flex items-center justify-center font-bold shadow-lg shadow-sky-500/30">
              <Award className="w-6 h-6" />
            </div>
          </div>
        </div>

        {/* Stats strip */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 mt-8 pt-6 border-t border-white/10 text-left">
          <div>
            <p className="text-[11px] text-slate-400 uppercase font-semibold">Total Credits</p>
            <p className="text-lg font-bold text-white mt-0.5">{calculationResult.totalCredits}</p>
          </div>
          <div>
            <p className="text-[11px] text-slate-400 uppercase font-semibold">Passed Credits</p>
            <p className="text-lg font-bold text-emerald-400 mt-0.5">{calculationResult.passedCredits}</p>
          </div>
          <div>
            <p className="text-[11px] text-slate-400 uppercase font-semibold">Total Subjects</p>
            <p className="text-lg font-bold text-white mt-0.5">{calculationResult.totalSubjects}</p>
          </div>
          <div>
            <p className="text-[11px] text-slate-400 uppercase font-semibold">Arrears</p>
            <p
              className={`text-lg font-bold mt-0.5 ${
                calculationResult.arrearCount > 0 ? 'text-amber-400' : 'text-emerald-400'
              }`}
            >
              {calculationResult.arrearCount === 0 ? 'None (Clear)' : `${calculationResult.arrearCount} Pending`}
            </p>
          </div>
        </div>
      </div>

      {/* Evaluated Subject Breakdown Table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div className="p-4 sm:p-6 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h3 className="text-base font-bold text-slate-800">Evaluated Subject Breakdown</h3>
            <p className="text-xs text-slate-400">
              Grade points and credit-weighted contribution for each course
            </p>
          </div>
          <span
            className={`px-3 py-1 rounded-full text-xs font-bold ${
              calculationResult.arrearCount === 0
                ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                : 'bg-amber-50 text-amber-700 border border-amber-200'
            }`}
          >
            {calculationResult.status}
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-200/80 text-[11px] font-semibold uppercase tracking-wider text-slate-400">
                <th className="py-3 px-4 w-12">#</th>
                <th className="py-3 px-4 w-32">Code</th>
                <th className="py-3 px-4">Subject Name</th>
                <th className="py-3 px-4 text-center w-20">Grade</th>
                <th className="py-3 px-4 text-center w-24">Grade Pt</th>
                <th className="py-3 px-4 text-center w-24">Credits</th>
                <th className="py-3 px-4 text-center w-28">Weighted Pts</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {calculationResult.subjects.map((sub, idx) => {
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
                            : 'bg-brand-50 text-brand-700'
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
                  Totals
                </td>
                <td className="py-3 px-4 text-center text-slate-800">{calculationResult.totalCredits}</td>
                <td className="py-3 px-4 text-center text-brand-700 font-mono">
                  {calculationResult.totalGradePoints.toFixed(1)}
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>

      {/* Action Buttons */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4 pt-4">
        <Link
          to="/"
          className="inline-flex items-center space-x-2 text-xs font-semibold text-slate-500 hover:text-slate-800 px-4 py-2.5 rounded-xl border border-slate-200 hover:bg-white transition-colors"
        >
          <LayoutDashboard className="w-4 h-4" />
          <span>Back to Dashboard</span>
        </Link>

        <div className="flex items-center space-x-3 w-full sm:w-auto">
          <button
            type="button"
            onClick={handleSaveSemester}
            disabled={isSaving || saveSuccess}
            className={`w-full sm:w-auto inline-flex items-center justify-center space-x-2 px-6 py-3 rounded-xl font-semibold text-sm shadow-md transition-all active:scale-95 ${
              saveSuccess
                ? 'bg-emerald-600 text-white cursor-default'
                : 'bg-brand-600 hover:bg-brand-700 text-white shadow-brand-600/25'
            }`}
          >
            {isSaving ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                <span>Saving to History...</span>
              </>
            ) : saveSuccess ? (
              <>
                <BookmarkCheck className="w-4 h-4" />
                <span>Saved to Semester History</span>
              </>
            ) : (
              <>
                <BookmarkCheck className="w-4 h-4" />
                <span>Save to Semester History</span>
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};


import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import { GpaSummary, SemesterRecord } from '../types';
import {
  Upload,
  TrendingUp,
  Award,
  BookOpen,
  Calendar,
  AlertCircle,
  ArrowRight,
  Sparkles,
  ChevronRight,
  Loader2,
  FileCheck2,
} from 'lucide-react';
import {
  ResponsiveContainer,
  AreaChart,
  Area,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
} from 'recharts';

export const DashboardPage: React.FC = () => {
  const { currentUser } = useAuth();
  const [summary, setSummary] = useState<GpaSummary | null>(null);
  const [semesters, setSemesters] = useState<SemesterRecord[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [sumData, semData] = await Promise.all([
        api.getGpaSummary().catch(() => null),
        api.getSemesters().catch(() => []),
      ]);
      if (sumData) setSummary(sumData);
      if (semData) setSemesters(semData);
    } catch (e: any) {
      console.error('Failed to load dashboard data:', e);
      setError('Unable to load semester analytics.');
    } finally {
      setLoading(false);
    }
  };

  const chartData = (summary?.history || semesters.map(s => ({
    semesterNumber: s.semesterNumber,
    sgpa: s.sgpa,
    academicYear: s.academicYear,
  }))).map(item => ({
    name: `Sem ${item.semesterNumber}`,
    sgpa: item.sgpa,
    academicYear: item.academicYear,
  }));

  const latestSemester = semesters.length > 0 ? semesters[semesters.length - 1] : null;

  return (
    <div className="space-y-8 animate-fadeIn font-poppins pb-10">
      {/* Welcome Banner in Deep Blue / Navy */}
      <div className="bg-gradient-to-r from-slate-950 via-slate-900 to-blue-950 rounded-3xl p-6 sm:p-9 text-white shadow-xl shadow-slate-950/15 flex flex-col md:flex-row items-start md:items-center justify-between gap-6 relative overflow-hidden border border-blue-900/40">
        <div className="absolute right-0 top-0 w-80 h-80 bg-sky-500/10 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10">
          <div className="inline-flex items-center space-x-2 px-3.5 py-1 rounded-full bg-blue-500/20 text-sky-300 text-xs font-semibold mb-3 border border-sky-400/30">
            <Sparkles className="w-3.5 h-3.5 text-sky-300" />
            <span>Academic Performance Dashboard</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight">
            Welcome, {currentUser?.displayName || 'Student'}
          </h1>
          <p className="text-slate-300 text-xs sm:text-sm mt-1 max-w-xl font-normal leading-relaxed">
            Rajalakshmi Institute of Technology • Chennai
            {latestSemester?.course && ` • ${latestSemester.course}`}
            {latestSemester?.registerNumber && ` • Reg: ${latestSemester.registerNumber}`}
          </p>
        </div>

        <Link
          to="/upload"
          className="relative z-10 inline-flex items-center space-x-2.5 px-6 py-3.5 rounded-2xl bg-blue-600 hover:bg-blue-500 text-white font-bold text-xs sm:text-sm shadow-lg shadow-blue-600/30 transition-all duration-200 active:scale-95 flex-shrink-0 cursor-pointer"
        >
          <Upload className="w-4 h-4" />
          <span>Upload Result PDF</span>
        </Link>
      </div>

      {loading ? (
        <div className="py-24 flex flex-col items-center justify-center">
          <Loader2 className="w-9 h-9 text-blue-600 animate-spin mb-3" />
          <p className="text-xs text-slate-400 font-medium">Loading academic records...</p>
        </div>
      ) : error ? (
        <div className="p-4 bg-red-50 border border-red-200 rounded-2xl flex items-center space-x-3 text-xs text-red-700">
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      ) : semesters.length === 0 ? (
        /* Empty State */
        <div className="bg-white rounded-3xl border border-dashed border-slate-300 p-12 sm:p-16 text-center shadow-soft">
          <div className="w-16 h-16 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center mx-auto mb-4 border border-blue-100">
            <Upload className="w-8 h-8" />
          </div>
          <h3 className="text-lg font-bold text-slate-900 font-poppins">No Semester Results Uploaded Yet</h3>
          <p className="text-xs sm:text-sm text-slate-500 max-w-md mx-auto mt-2 leading-relaxed">
            Upload your official RIT semester result PDF to automatically extract courses, enter credits, and compute your official SGPA and cumulative CGPA.
          </p>
          <div className="mt-6">
            <Link
              to="/upload"
              className="inline-flex items-center space-x-2 px-6 py-3.5 rounded-2xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs shadow-md shadow-blue-600/20 transition-all active:scale-95"
            >
              <Upload className="w-4 h-4" />
              <span>Upload Your First Result PDF</span>
            </Link>
          </div>
        </div>
      ) : (
        /* Metric Cards in Blue & Light Blue Palette */
        <>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
            {/* Latest SGPA */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-card hover:shadow-elevated transition-shadow duration-200 flex items-center justify-between">
              <div>
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                  Latest SGPA
                </p>
                <p className="text-3xl sm:text-4xl font-extrabold text-blue-700 mt-1 font-poppins">
                  {summary?.latestSgpa !== null && summary?.latestSgpa !== undefined
                    ? summary.latestSgpa.toFixed(2)
                    : '—'}
                </p>
                <p className="text-[11px] text-slate-500 mt-1 font-medium">
                  Semester {latestSemester?.semesterNumber || '—'}
                </p>
              </div>
              <div className="w-13 h-13 rounded-2xl bg-blue-50 text-blue-600 flex items-center justify-center border border-blue-100">
                <Award className="w-6 h-6" />
              </div>
            </div>

            {/* Overall CGPA */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-card hover:shadow-elevated transition-shadow duration-200 flex items-center justify-between">
              <div>
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                  Overall CGPA
                </p>
                <p className="text-3xl sm:text-4xl font-extrabold text-slate-900 mt-1 font-poppins">
                  {summary?.overallCgpa !== null && summary?.overallCgpa !== undefined
                    ? summary.overallCgpa.toFixed(2)
                    : '—'}
                </p>
                <p className="text-[11px] text-slate-500 mt-1 font-medium">
                  Cumulative across semesters
                </p>
              </div>
              <div className="w-13 h-13 rounded-2xl bg-sky-50 text-sky-600 flex items-center justify-center border border-sky-100">
                <TrendingUp className="w-6 h-6" />
              </div>
            </div>

            {/* Completed Semesters */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-card hover:shadow-elevated transition-shadow duration-200 flex items-center justify-between">
              <div>
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                  Semesters
                </p>
                <p className="text-3xl sm:text-4xl font-extrabold text-slate-900 mt-1 font-poppins">
                  {summary?.completedSemesters ?? semesters.length}
                </p>
                <p className="text-[11px] text-slate-500 mt-1 font-medium">
                  Records evaluated
                </p>
              </div>
              <div className="w-13 h-13 rounded-2xl bg-indigo-50 text-indigo-600 flex items-center justify-center border border-indigo-100">
                <Calendar className="w-6 h-6" />
              </div>
            </div>

            {/* Total Credits */}
            <div className="bg-white rounded-2xl p-6 border border-slate-200 shadow-card hover:shadow-elevated transition-shadow duration-200 flex items-center justify-between">
              <div>
                <p className="text-[11px] font-bold text-slate-400 uppercase tracking-wider">
                  Earned Credits
                </p>
                <p className="text-3xl sm:text-4xl font-extrabold text-slate-900 mt-1 font-poppins">
                  {summary?.totalPassedCredits ?? '—'}
                </p>
                <p className="text-[11px] text-slate-500 mt-1 font-medium">
                  of {summary?.totalCredits ?? '—'} registered
                </p>
              </div>
              <div className="w-13 h-13 rounded-2xl bg-slate-50 text-slate-600 flex items-center justify-center border border-slate-200">
                <BookOpen className="w-6 h-6" />
              </div>
            </div>
          </div>

          {/* GPA Trend Chart (Blue Palette) */}
          {chartData.length > 0 && (
            <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-card">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <h3 className="text-base sm:text-lg font-bold text-slate-900 font-poppins">GPA Progression Trajectory</h3>
                  <p className="text-xs text-slate-400 font-medium">Semester-by-semester SGPA performance curve</p>
                </div>
                <div className="flex items-center space-x-2 text-xs font-semibold text-blue-700 bg-blue-50 px-3 py-1.5 rounded-xl border border-blue-200">
                  <TrendingUp className="w-3.5 h-3.5 text-blue-600" />
                  <span>Performance Curve</span>
                </div>
              </div>
              <div className="h-64 sm:h-72 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={chartData} margin={{ top: 10, right: 20, left: -10, bottom: 0 }}>
                    <defs>
                      <linearGradient id="colorSgpa" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#0284c7" stopOpacity={0.25} />
                        <stop offset="95%" stopColor="#0284c7" stopOpacity={0.0} />
                      </linearGradient>
                    </defs>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                    <XAxis
                      dataKey="name"
                      stroke="#94a3b8"
                      fontSize={11}
                      tickLine={false}
                      fontFamily="Poppins"
                    />
                    <YAxis
                      domain={[0, 10]}
                      stroke="#94a3b8"
                      fontSize={11}
                      tickLine={false}
                      fontFamily="Poppins"
                    />
                    <Tooltip
                      contentStyle={{
                        backgroundColor: '#0f172a',
                        borderRadius: '14px',
                        color: '#fff',
                        fontSize: '12px',
                        fontFamily: 'Poppins',
                        border: 'none',
                        boxShadow: '0 10px 25px -5px rgba(0, 0, 0, 0.3)',
                      }}
                      formatter={(value: any) => [`${Number(value).toFixed(2)}`, 'SGPA']}
                    />
                    <Area
                      type="monotone"
                      dataKey="sgpa"
                      stroke="#0284c7"
                      strokeWidth={3}
                      fillOpacity={1}
                      fill="url(#colorSgpa)"
                      dot={{ fill: '#0284c7', r: 5, strokeWidth: 2, stroke: '#ffffff' }}
                      activeDot={{ r: 7 }}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </div>
          )}

          {/* Recent Semester History Table */}
          <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200 shadow-card">
            <div className="flex items-center justify-between mb-5">
              <div>
                <h3 className="text-base sm:text-lg font-bold text-slate-900 font-poppins">Semester Records</h3>
                <p className="text-xs text-slate-400 font-medium">All registered and evaluated semesters</p>
              </div>
              <Link
                to="/history"
                className="text-xs font-bold text-blue-600 hover:text-blue-700 flex items-center space-x-1 transition-colors"
              >
                <span>View Full History</span>
                <ChevronRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            <div className="divide-y divide-slate-100">
              {semesters.map((sem) => (
                <div
                  key={sem.id || sem.semesterNumber}
                  className="py-4 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-blue-50/40 px-3 rounded-2xl transition-all duration-150"
                >
                  <div className="flex items-center space-x-4">
                    <div className="w-11 h-11 rounded-2xl bg-blue-50 text-blue-800 font-bold flex items-center justify-center text-xs font-poppins border border-blue-100">
                      S{sem.semesterNumber}
                    </div>
                    <div>
                      <p className="text-xs sm:text-sm font-bold text-slate-900 font-poppins">
                        Semester {sem.semesterNumber}{' '}
                        {sem.academicYear && (
                          <span className="text-xs font-normal text-slate-400">
                            • {sem.academicYear}
                          </span>
                        )}
                      </p>
                      <p className="text-[11px] text-slate-400 font-medium mt-0.5">
                        {sem.subjects?.length || 0} Subjects • {sem.totalCredits} Registered Credits
                      </p>
                    </div>
                  </div>

                  <div className="flex items-center space-x-6">
                    <div className="text-right">
                      <p className="text-xs sm:text-sm font-bold text-slate-900 font-poppins">
                        SGPA: <span className="text-blue-600 font-extrabold">{sem.sgpa?.toFixed(2)}</span>
                      </p>
                      <p className="text-[11px] mt-0.5">
                        {sem.arrearCount > 0 ? (
                          <span className="text-amber-700 font-semibold bg-amber-50 px-2.5 py-0.5 rounded-full border border-amber-200">
                            {sem.arrearCount} Arrear(s)
                          </span>
                        ) : (
                          <span className="text-blue-700 font-semibold bg-blue-50 px-2.5 py-0.5 rounded-full border border-blue-200">
                            All Cleared
                          </span>
                        )}
                      </p>
                    </div>
                    <Link
                      to={`/semesters/${sem.id || sem.semesterNumber}`}
                      className="p-2.5 rounded-xl text-slate-400 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                      title="View Breakdown"
                    >
                      <ArrowRight className="w-4 h-4" />
                    </Link>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </>
      )}
    </div>
  );
};

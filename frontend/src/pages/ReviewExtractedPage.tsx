import React, { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ExtractedResultResponse, SubjectGradeInput } from '../types';
import { api } from '../services/api';
import {
  Calculator,
  Plus,
  Trash2,
  AlertTriangle,
  FileText,
  User,
  Hash,
  Calendar,
  BookOpen,
  ArrowRight,
  RotateCcw,
  Loader2,
} from 'lucide-react';

export const ReviewExtractedPage: React.FC = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const extractedData = (location.state as { extractedData?: ExtractedResultResponse })?.extractedData;

  const [studentName, setStudentName] = useState<string>(extractedData?.studentName || '');
  const [registerNumber, setRegisterNumber] = useState<string>(extractedData?.registerNumber || '');
  const [academicYear, setAcademicYear] = useState<string>(extractedData?.academicYear || '');
  const [semesterNumber, setSemesterNumber] = useState<string>(extractedData?.semester || '04');
  const [course, setCourse] = useState<string>(extractedData?.course || '');

  // Initialize subjects table with user credit inputs empty (per project rule: credits must never be guessed)
  const [subjects, setSubjects] = useState<SubjectGradeInput[]>(() => {
    if (!extractedData?.subjects) return [];
    return extractedData.subjects.map((s) => ({
      subjectCode: s.subjectCode || '',
      subjectName: s.subjectName || '',
      grade: s.grade || 'O',
      credits: ('' as unknown) as number, // Student must enter
      resultStatus: s.resultStatus || 'PASS',
    }));
  });

  const [validationErrors, setValidationErrors] = useState<string[]>([]);
  const [isCalculating, setIsCalculating] = useState<boolean>(false);

  const GRADE_OPTIONS = ['O', 'A+', 'A', 'B+', 'B', 'C', 'U', 'RA', 'SA', 'W'];

  const handleSubjectChange = (index: number, field: keyof SubjectGradeInput, value: any) => {
    const updated = [...subjects];
    if (field === 'credits') {
      const num = value === '' ? ('' as unknown as number) : parseFloat(value);
      updated[index] = { ...updated[index], credits: num };
    } else {
      updated[index] = { ...updated[index], [field]: value };
    }
    setSubjects(updated);
  };

  const handleAddRow = () => {
    setSubjects([
      ...subjects,
      {
        subjectCode: '',
        subjectName: '',
        grade: 'O',
        credits: ('' as unknown) as number,
        resultStatus: 'PASS',
      },
    ]);
  };

  const handleRemoveRow = (index: number) => {
    if (subjects.length <= 1) {
      alert('You must have at least one subject in the semester.');
      return;
    }
    const updated = subjects.filter((_, i) => i !== index);
    setSubjects(updated);
  };

  const validateInputs = (): boolean => {
    const errors: string[] = [];
    if (subjects.length === 0) {
      errors.push('At least one subject is required.');
    }

    subjects.forEach((s, idx) => {
      const rowNum = idx + 1;
      if (!s.subjectCode || !s.subjectCode.trim()) {
        errors.push(`Row ${rowNum}: Subject Code is required.`);
      }
      if (!s.subjectName || !s.subjectName.trim()) {
        errors.push(`Row ${rowNum}: Subject Name is required.`);
      }
      if (!s.grade || !s.grade.trim()) {
        errors.push(`Row ${rowNum}: Grade is required.`);
      }
      if (s.credits === undefined || s.credits === null || isNaN(Number(s.credits)) || Number(s.credits) <= 0) {
        errors.push(`Row ${rowNum}: Please enter valid positive credits (e.g. 1.0, 2.0, 3.0, 4.0).`);
      }
    });

    setValidationErrors(errors);
    return errors.length === 0;
  };

  const handleConfirmAndCalculate = async () => {
    if (!validateInputs()) return;

    setIsCalculating(true);
    try {
      const formattedSubjects: SubjectGradeInput[] = subjects.map((s) => ({
        ...s,
        credits: Number(s.credits),
      }));

      const requestPayload = {
        studentName,
        registerNumber,
        academicYear,
        semesterNumber,
        course,
        subjects: formattedSubjects,
      };

      const result = await api.calculateGpa(requestPayload);

      // Navigate to results summary screen
      navigate('/result', {
        state: {
          calculationResult: result,
          semesterInfo: {
            studentName,
            registerNumber,
            academicYear,
            semesterNumber,
            course,
          },
        },
      });
    } catch (err: any) {
      console.error('Calculation failed:', err);
      const msg = err?.response?.data?.message || err?.message || 'Failed to calculate GPA.';
      setValidationErrors([msg]);
    } finally {
      setIsCalculating(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto space-y-6 animate-fadeIn pb-12">
      {/* Title & instructions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-800">Review & Enter Subject Credits</h1>
          <p className="text-sm text-slate-500 mt-1">
            Subjects and grades extracted from your RIT result PDF. Please review details and enter credits for each course.
          </p>
        </div>
        <button
          onClick={() => navigate('/upload')}
          className="inline-flex items-center space-x-1.5 px-3 py-2 text-xs font-semibold text-slate-500 hover:text-slate-800 bg-white border border-slate-200 rounded-xl hover:bg-slate-50 transition-colors self-start"
        >
          <RotateCcw className="w-3.5 h-3.5" />
          <span>Upload Another PDF</span>
        </button>
      </div>

      {/* Warnings Banner if any */}
      {extractedData?.warnings && extractedData.warnings.length > 0 && (
        <div className="p-4 bg-amber-50 border border-amber-200 rounded-2xl flex items-start space-x-3 text-xs text-amber-800">
          <AlertTriangle className="w-4 h-4 text-amber-600 flex-shrink-0 mt-0.5" />
          <div>
            <span className="font-semibold">Extraction Notice: </span>
            {extractedData.warnings.join(' • ')}
          </div>
        </div>
      )}

      {/* Student & Semester Details Card */}
      <div className="bg-white rounded-2xl border border-slate-200/80 p-6 shadow-sm">
        <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-4 flex items-center space-x-2">
          <FileText className="w-4 h-4 text-brand-600" />
          <span>Extracted Student Details</span>
        </h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
          <div>
            <label className="text-[11px] font-semibold text-slate-400 uppercase">Student Name</label>
            <input
              type="text"
              value={studentName}
              onChange={(e) => setStudentName(e.target.value)}
              className="mt-1 w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
            />
          </div>
          <div>
            <label className="text-[11px] font-semibold text-slate-400 uppercase">Register Number</label>
            <input
              type="text"
              value={registerNumber}
              onChange={(e) => setRegisterNumber(e.target.value)}
              className="mt-1 w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
            />
          </div>
          <div>
            <label className="text-[11px] font-semibold text-slate-400 uppercase">Academic Year</label>
            <input
              type="text"
              value={academicYear}
              onChange={(e) => setAcademicYear(e.target.value)}
              className="mt-1 w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
            />
          </div>
          <div>
            <label className="text-[11px] font-semibold text-slate-400 uppercase">Semester</label>
            <input
              type="text"
              value={semesterNumber}
              onChange={(e) => setSemesterNumber(e.target.value)}
              className="mt-1 w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
            />
          </div>
          <div>
            <label className="text-[11px] font-semibold text-slate-400 uppercase">Course / Branch</label>
            <input
              type="text"
              value={course}
              onChange={(e) => setCourse(e.target.value)}
              className="mt-1 w-full text-xs font-medium px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500"
            />
          </div>
        </div>
      </div>

      {/* Validation Errors Box */}
      {validationErrors.length > 0 && (
        <div className="p-4 bg-red-50 border border-red-200 rounded-2xl text-xs text-red-700 space-y-1">
          <p className="font-semibold flex items-center space-x-1.5">
            <AlertTriangle className="w-4 h-4 text-red-600" />
            <span>Please correct the following:</span>
          </p>
          <ul className="list-disc list-inside space-y-0.5 pl-2">
            {validationErrors.map((err, i) => (
              <li key={i}>{err}</li>
            ))}
          </ul>
        </div>
      )}

      {/* Extracted Subjects Table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden">
        <div className="p-4 sm:p-6 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h3 className="text-base font-bold text-slate-800">Subject Results Table</h3>
            <p className="text-xs text-slate-400">
              {subjects.length} subject(s) extracted • Enter credits for each row
            </p>
          </div>
          <button
            type="button"
            onClick={handleAddRow}
            className="inline-flex items-center space-x-1.5 px-3 py-1.5 text-xs font-semibold text-brand-700 bg-brand-50 hover:bg-brand-100 rounded-xl transition-colors"
          >
            <Plus className="w-3.5 h-3.5" />
            <span>Add Row</span>
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50/70 border-b border-slate-200/80 text-[11px] font-semibold uppercase tracking-wider text-slate-400">
                <th className="py-3 px-4 w-12">#</th>
                <th className="py-3 px-4 w-32">Subject Code</th>
                <th className="py-3 px-4">Subject Title</th>
                <th className="py-3 px-4 w-28">Grade</th>
                <th className="py-3 px-4 w-28">Result</th>
                <th className="py-3 px-4 w-36">Credits *</th>
                <th className="py-3 px-4 w-16 text-center">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-xs">
              {subjects.map((sub, idx) => (
                <tr key={idx} className="hover:bg-slate-50/50 transition-colors">
                  <td className="py-3 px-4 font-mono text-slate-400 text-center">{idx + 1}</td>
                  <td className="py-3 px-4 font-mono font-medium text-slate-800">
                    <input
                      type="text"
                      value={sub.subjectCode}
                      onChange={(e) => handleSubjectChange(idx, 'subjectCode', e.target.value)}
                      className="w-full px-2.5 py-1.5 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-brand-500 font-mono text-xs uppercase"
                    />
                  </td>
                  <td className="py-3 px-4 font-medium text-slate-700">
                    <input
                      type="text"
                      value={sub.subjectName}
                      onChange={(e) => handleSubjectChange(idx, 'subjectName', e.target.value)}
                      className="w-full px-2.5 py-1.5 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-brand-500 text-xs"
                    />
                  </td>
                  <td className="py-3 px-4">
                    <select
                      value={sub.grade}
                      onChange={(e) => handleSubjectChange(idx, 'grade', e.target.value)}
                      className="w-full px-2.5 py-1.5 bg-slate-50 border border-slate-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-brand-500 font-bold text-xs"
                    >
                      {GRADE_OPTIONS.map((g) => (
                        <option key={g} value={g}>
                          {g}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td className="py-3 px-4">
                    <span
                      className={`inline-block px-2.5 py-1 rounded-md text-[11px] font-bold ${
                        sub.grade === 'U' || sub.grade === 'RA'
                          ? 'bg-amber-100 text-amber-800'
                          : 'bg-emerald-100 text-emerald-800'
                      }`}
                    >
                      {sub.grade === 'U' || sub.grade === 'RA' ? 'RA' : 'PASS'}
                    </span>
                  </td>
                  <td className="py-3 px-4">
                    <input
                      type="number"
                      step="0.5"
                      min="0.5"
                      max="10"
                      placeholder="e.g. 3.0"
                      value={sub.credits === undefined || sub.credits === null ? '' : sub.credits}
                      onChange={(e) => handleSubjectChange(idx, 'credits', e.target.value)}
                      className="w-full px-2.5 py-1.5 bg-blue-50/40 border border-sky-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500 font-semibold text-slate-800 text-xs text-center"
                    />
                  </td>
                  <td className="py-3 px-4 text-center">
                    <button
                      type="button"
                      onClick={() => handleRemoveRow(idx)}
                      className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                      title="Remove Row"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Calculation Confirmation */}
      <div className="flex items-center justify-between pt-4">
        <p className="text-xs text-slate-400">
          Calculation applies official credit-weighted formula:{' '}
          <span className="font-mono text-slate-600">SGPA = Σ(Credit × Grade Point) / Σ(Credits)</span>
        </p>
        <button
          type="button"
          onClick={handleConfirmAndCalculate}
          disabled={isCalculating}
          className="inline-flex items-center space-x-2 px-6 py-3 rounded-xl bg-brand-600 hover:bg-brand-700 text-white font-semibold text-sm shadow-md shadow-brand-600/25 transition-all active:scale-95 disabled:opacity-50"
        >
          {isCalculating ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              <span>Calculating SGPA...</span>
            </>
          ) : (
            <>
              <Calculator className="w-4 h-4" />
              <span>Confirm & Calculate GPA</span>
              <ArrowRight className="w-4 h-4" />
            </>
          )}
        </button>
      </div>
    </div>
  );
};


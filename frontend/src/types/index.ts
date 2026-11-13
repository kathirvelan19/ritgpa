export interface ExtractedSubject {
  subjectSemester?: string;
  subjectCode: string;
  subjectName: string;
  grade: string;
  resultStatus: string;
  examDate?: string;
  suggestedCredits?: number | null;
  uncertain?: boolean;
  warningMessage?: string;
}

export interface ExtractedResultResponse {
  success: boolean;
  message: string;
  studentName?: string;
  registerNumber?: string;
  academicYear?: string;
  semester?: string;
  course?: string;
  subjects: ExtractedSubject[];
  warnings: string[];
}

export interface SubjectGradeInput {
  subjectCode: string;
  subjectName: string;
  grade: string;
  credits: number;
  resultStatus?: string;
  gradePoint?: number;
}

export interface CalculateGpaRequest {
  studentName?: string;
  registerNumber?: string;
  academicYear?: string;
  semesterNumber?: string;
  course?: string;
  subjects: SubjectGradeInput[];
}

export interface CalculateGpaResponse {
  sgpa: number;
  totalCredits: number;
  totalGradePoints: number;
  passedCredits: number;
  totalSubjects: number;
  arrearCount: number;
  subjects: SubjectGradeInput[];
  status: string;
}

export interface SemesterRecord {
  id?: string;
  studentUid?: string;
  studentName?: string;
  registerNumber?: string;
  academicYear?: string;
  semesterNumber: string;
  course?: string;
  subjects: SubjectGradeInput[];
  totalCredits: number;
  passedCredits: number;
  sgpa: number;
  cgpa?: number;
  arrearCount: number;
  extractionStatus?: string;
  createdAt?: number;
  updatedAt?: number;
}

export interface SemesterHistoryItem {
  semesterId: string;
  semesterNumber: string;
  academicYear: string;
  sgpa: number;
  credits: number;
  subjectCount: number;
  arrears: number;
}

export interface GpaSummary {
  latestSgpa: number | null;
  overallCgpa: number | null;
  totalCredits: number;
  totalPassedCredits: number;
  completedSemesters: number;
  totalArrears: number;
  history: SemesterHistoryItem[];
}


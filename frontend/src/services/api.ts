import axios from 'axios';
import { auth } from '../firebase/firebaseConfig';
import type {
  CalculateGpaRequest,
  CalculateGpaResponse,
  ExtractedResultResponse,
  GpaSummary,
  SemesterRecord,
} from '../types';

const API_BASE = '/api';

const apiClient = axios.create({
  baseURL: API_BASE,
});

apiClient.interceptors.request.use(async (config) => {
  const user = auth.currentUser;
  if (user) {
    try {
      const token = await user.getIdToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    } catch (e) {
      console.warn('Could not get Firebase ID token:', e);
    }
  }
  return config;
});

export const api = {
  extractPdf: async (
    file: File,
    onProgress?: (percent: number) => void
  ): Promise<ExtractedResultResponse> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post<ExtractedResultResponse>('/pdf/extract', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total && onProgress) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(percent);
        }
      },
    });
    return response.data;
  },

  calculateGpa: async (data: CalculateGpaRequest): Promise<CalculateGpaResponse> => {
    const response = await apiClient.post<CalculateGpaResponse>('/gpa/calculate', data);
    return response.data;
  },

  saveSemester: async (record: SemesterRecord): Promise<SemesterRecord> => {
    const response = await apiClient.post<SemesterRecord>('/semesters', record);
    return response.data;
  },

  getSemesters: async (): Promise<SemesterRecord[]> => {
    const response = await apiClient.get<SemesterRecord[]>('/semesters');
    return response.data;
  },

  getSemesterById: async (id: string): Promise<SemesterRecord> => {
    const response = await apiClient.get<SemesterRecord>(`/semesters/${id}`);
    return response.data;
  },

  getGpaSummary: async (): Promise<GpaSummary> => {
    const response = await apiClient.get<GpaSummary>('/gpa/summary');
    return response.data;
  },
};

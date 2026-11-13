import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { ExtractedResultResponse } from '../types';
import {
  Upload,
  FileText,
  AlertCircle,
  CheckCircle2,
  Loader2,
  RefreshCw,
  ArrowRight,
  ShieldAlert,
} from 'lucide-react';

export const UploadPage: React.FC = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [file, setFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState<boolean>(false);
  const [uploadProgress, setUploadProgress] = useState<number>(0);
  const [isExtracting, setIsExtracting] = useState<boolean>(false);
  const [extractionStage, setExtractionStage] = useState<string>('');
  const [error, setError] = useState<string | null>(null);

  const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

  const validateAndSetFile = (selectedFile: File) => {
    setError(null);
    if (!selectedFile.name.toLowerCase().endsWith('.pdf') && selectedFile.type !== 'application/pdf') {
      setError('Please select a valid PDF file. Other file formats are not supported.');
      return false;
    }
    if (selectedFile.size > MAX_FILE_SIZE) {
      setError('File size exceeds the 10MB limit. Please upload a smaller PDF file.');
      return false;
    }
    if (selectedFile.size === 0) {
      setError('Selected PDF file is empty (0 bytes).');
      return false;
    }
    setFile(selectedFile);
    return true;
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      validateAndSetFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      validateAndSetFile(e.target.files[0]);
    }
  };

  const handleUploadAndExtract = async () => {
    if (!file) return;

    setError(null);
    setIsExtracting(true);
    setUploadProgress(0);
    setExtractionStage('Uploading semester result PDF...');

    try {
      setExtractionStage('Sending to RIT PDFBox extraction engine...');
      const response: ExtractedResultResponse = await api.extractPdf(file, (percent) => {
        setUploadProgress(percent);
        if (percent === 100) {
          setExtractionStage('Parsing document text coordinates and subject table...');
        }
      });

      if (!response.success || response.subjects.length === 0) {
        setError(response.message || 'Could not parse result table from this document.');
        setIsExtracting(false);
        return;
      }

      setExtractionStage('Extraction successful! Redirecting to subject review...');
      setTimeout(() => {
        // Navigate to review screen with extracted data
        navigate('/review', { state: { extractedData: response } });
      }, 600);
    } catch (err: any) {
      console.error('Extraction error:', err);
      const errMsg =
        err?.response?.data?.message ||
        err?.message ||
        'Failed to extract semester results. Please ensure the document is an official RIT result PDF.';
      setError(errMsg);
      setIsExtracting(false);
    }
  };

  const handleReset = () => {
    setFile(null);
    setError(null);
    setUploadProgress(0);
    setIsExtracting(false);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-fadeIn">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold tracking-tight text-slate-800">Upload Semester Result</h1>
        <p className="text-sm text-slate-500 mt-1">
          Upload your official Rajalakshmi Institute of Technology result PDF to automatically extract subjects and grades.
        </p>
      </div>

      {/* Main Upload Card */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm p-6 sm:p-8">
        {!file ? (
          /* Drag & Drop Box */
          <div
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onClick={() => fileInputRef.current?.click()}
            className={`border-2 border-dashed rounded-2xl p-10 text-center cursor-pointer transition-all duration-200 ${
              isDragging
                ? 'border-brand-500 bg-brand-50/50 scale-[1.01]'
                : 'border-slate-200 hover:border-brand-400 hover:bg-slate-50/60'
            }`}
          >
            <input
              ref={fileInputRef}
              type="file"
              accept=".pdf,application/pdf"
              className="hidden"
              onChange={handleFileChange}
            />
            <div className="w-14 h-14 rounded-2xl bg-blue-50 text-brand-600 flex items-center justify-center mx-auto mb-4">
              <Upload className="w-7 h-7" />
            </div>
            <h3 className="text-base font-bold text-slate-800">
              Drag & Drop your RIT Result PDF here
            </h3>
            <p className="text-xs text-slate-400 mt-1">or click to browse files from your computer</p>
            <div className="mt-4 inline-flex items-center space-x-2 px-3 py-1 bg-slate-100 rounded-lg text-xs text-slate-500 font-medium">
              <span>PDF up to 10MB</span>
            </div>
          </div>
        ) : (
          /* Selected File State */
          <div className="space-y-6">
            <div className="flex items-center justify-between p-4 bg-slate-50 rounded-xl border border-slate-200">
              <div className="flex items-center space-x-3 truncate">
                <div className="w-10 h-10 rounded-xl bg-blue-100 text-brand-700 flex items-center justify-center flex-shrink-0">
                  <FileText className="w-5 h-5" />
                </div>
                <div className="truncate">
                  <p className="text-sm font-semibold text-slate-800 truncate">{file.name}</p>
                  <p className="text-xs text-slate-400">{formatFileSize(file.size)}</p>
                </div>
              </div>
              {!isExtracting && (
                <button
                  onClick={handleReset}
                  className="text-xs font-semibold text-slate-400 hover:text-red-600 px-3 py-1.5 rounded-lg hover:bg-red-50 transition-colors"
                >
                  Change File
                </button>
              )}
            </div>

            {/* Extraction Progress */}
            {isExtracting && (
              <div className="space-y-3 p-4 bg-blue-50/50 rounded-xl border border-blue-100">
                <div className="flex items-center justify-between text-xs font-medium text-brand-800">
                  <span className="flex items-center space-x-2">
                    <Loader2 className="w-4 h-4 animate-spin text-brand-600" />
                    <span>{extractionStage}</span>
                  </span>
                  <span>{uploadProgress}%</span>
                </div>
                <div className="w-full bg-slate-200 rounded-full h-2 overflow-hidden">
                  <div
                    className="bg-brand-600 h-2 rounded-full transition-all duration-300"
                    style={{ width: `${uploadProgress}%` }}
                  />
                </div>
              </div>
            )}

            {/* Action Buttons */}
            {!isExtracting && (
              <div className="flex items-center justify-end space-x-3 pt-2">
                <button
                  type="button"
                  onClick={handleReset}
                  className="px-4 py-2.5 rounded-xl border border-slate-200 text-slate-600 text-sm font-medium hover:bg-slate-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleUploadAndExtract}
                  className="inline-flex items-center space-x-2 px-6 py-2.5 rounded-xl bg-brand-600 hover:bg-brand-700 text-white font-semibold text-sm shadow-md shadow-brand-600/20 transition-all active:scale-95"
                >
                  <span>Extract Subjects</span>
                  <ArrowRight className="w-4 h-4" />
                </button>
              </div>
            )}
          </div>
        )}

        {/* Error notification & Retry */}
        {error && (
          <div className="mt-6 p-4 bg-red-50 border border-red-200 rounded-xl space-y-3">
            <div className="flex items-start space-x-3 text-red-800">
              <ShieldAlert className="w-5 h-5 flex-shrink-0 mt-0.5 text-red-600" />
              <div>
                <p className="text-sm font-semibold">Extraction Failed</p>
                <p className="text-xs text-red-700 mt-1">{error}</p>
              </div>
            </div>
            <div className="flex justify-end">
              <button
                onClick={handleUploadAndExtract}
                className="inline-flex items-center space-x-1.5 px-3 py-1.5 bg-red-100 hover:bg-red-200 text-red-800 rounded-lg text-xs font-semibold transition-colors"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Retry Extraction</span>
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Guidelines Box */}
      <div className="bg-slate-50 rounded-2xl border border-slate-200/80 p-5 space-y-2">
        <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500">
          Supported Document Format
        </h4>
        <ul className="text-xs text-slate-500 space-y-1 list-disc list-inside">
          <li>Rajalakshmi Institute of Technology Semester Results PDF.</li>
          <li>Ensure the PDF includes the student register number, academic year, and result table.</li>
          <li>Subject credits are entered by you in the next step according to your curriculum.</li>
        </ul>
      </div>
    </div>
  );
};


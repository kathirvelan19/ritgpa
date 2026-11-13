import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { LoginPage } from './pages/LoginPage';
import { DashboardPage } from './pages/DashboardPage';
import { UploadPage } from './pages/UploadPage';
import { ReviewExtractedPage } from './pages/ReviewExtractedPage';
import { ResultSummaryPage } from './pages/ResultSummaryPage';
import { HistoryPage } from './pages/HistoryPage';
import { SemesterDetailPage } from './pages/SemesterDetailPage';

export const App: React.FC = () => {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Login Route */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected Application Routes */}
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<DashboardPage />} />
            <Route path="/upload" element={<UploadPage />} />
            <Route path="/review" element={<ReviewExtractedPage />} />
            <Route path="/result" element={<ResultSummaryPage />} />
            <Route path="/history" element={<HistoryPage />} />
            <Route path="/semesters/:id" element={<SemesterDetailPage />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
};

export default App;


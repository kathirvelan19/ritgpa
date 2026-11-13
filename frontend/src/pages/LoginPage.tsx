import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  FileCheck2,
  Calculator,
  ShieldCheck,
  AlertCircle,
  ArrowRight,
  GraduationCap,
} from 'lucide-react';

export const LoginPage: React.FC = () => {
  const { loginWithGoogle, currentUser } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const [isSigningIn, setIsSigningIn] = useState<boolean>(false);

  React.useEffect(() => {
    if (currentUser) {
      navigate('/', { replace: true });
    }
  }, [currentUser, navigate]);

  const handleGoogleSignIn = async () => {
    setError(null);
    setIsSigningIn(true);
    try {
      await loginWithGoogle();
      navigate('/', { replace: true });
    } catch (err: any) {
      console.error('Google Sign-In Error:', err);
      if (err?.code === 'auth/popup-closed-by-user' || err?.code === 'auth/cancelled-popup-request') {
        setError('Sign-in window was closed. Click the button below to try again.');
      } else if (err?.code === 'auth/popup-blocked') {
        setError('Your browser blocked the Google sign-in popup. Please allow popups for localhost and try again.');
      } else {
        setError(err?.message || 'Google Sign-In failed. Please try again.');
      }
    } finally {
      setIsSigningIn(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-center items-center bg-gradient-to-br from-slate-950 via-slate-900 to-blue-950 p-4 sm:p-6 lg:p-8 relative overflow-hidden font-poppins selection:bg-blue-600 selection:text-white">
      {/* Background ambient decorative blurs in blue */}
      <div className="absolute top-[-10%] left-[-5%] w-96 h-96 bg-blue-600/15 rounded-full blur-3xl pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-5%] w-96 h-96 bg-sky-500/15 rounded-full blur-3xl pointer-events-none" />

      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl shadow-slate-950/40 border border-slate-100 overflow-hidden relative z-10 animate-fadeIn">
        {/* Top Accent Strip in Royal Blue & Sky */}
        <div className="h-2.5 bg-gradient-to-r from-blue-700 via-sky-500 to-blue-600" />

        <div className="p-8 sm:p-10">
          {/* Official College Logo Header */}
          <div className="text-center mb-8">
            <div className="inline-block p-2.5 bg-white rounded-2xl border border-slate-200 shadow-sm mb-4">
              <img
                src="/rit-logo.png"
                alt="Rajalakshmi Institute of Technology"
                className="h-16 w-auto object-contain mx-auto"
              />
            </div>

            <h1 className="text-2xl sm:text-3xl font-extrabold tracking-tight text-slate-900 font-poppins">
              RIT <span className="text-blue-600">GPA</span> Calculator
            </h1>
            <p className="text-xs text-slate-500 mt-2 max-w-xs mx-auto leading-relaxed">
              Official academic performance portal for automated result PDF parsing and verified SGPA & CGPA calculations.
            </p>
          </div>

          {/* Error Notice */}
          {error && (
            <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-2xl flex items-start space-x-3 text-xs text-red-700 animate-fadeIn">
              <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0 text-red-600" />
              <span className="leading-relaxed font-medium">{error}</span>
            </div>
          )}

          {/* Google Sign In Button */}
          <button
            onClick={handleGoogleSignIn}
            disabled={isSigningIn}
            className="w-full flex items-center justify-center space-x-3 py-4 px-5 bg-white border border-slate-300 hover:border-blue-400 rounded-2xl font-semibold text-slate-700 hover:bg-blue-50/40 shadow-sm hover:shadow-md transition-all duration-200 active:scale-[0.99] disabled:opacity-50 group cursor-pointer"
          >
            <svg className="w-5 h-5 flex-shrink-0" viewBox="0 0 24 24">
              <path
                fill="#4285F4"
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
              />
              <path
                fill="#34A853"
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
              />
              <path
                fill="#FBBC05"
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"
              />
              <path
                fill="#EA4335"
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"
              />
            </svg>
            <span className="text-sm font-semibold tracking-wide">
              {isSigningIn ? 'Connecting to Google...' : 'Sign in with Google'}
            </span>
            <ArrowRight className="w-4 h-4 text-slate-400 group-hover:translate-x-1 group-hover:text-blue-600 transition-all ml-auto" />
          </button>

          {/* Workflow highlights in Blue / Sky */}
          <div className="mt-8 pt-6 border-t border-slate-100 space-y-3.5">
            <div className="flex items-center space-x-3 text-xs text-slate-600 font-medium">
              <div className="w-7 h-7 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0 border border-blue-100">
                <FileCheck2 className="w-4 h-4" />
              </div>
              <span>Upload official RIT semester result PDF</span>
            </div>
            <div className="flex items-center space-x-3 text-xs text-slate-600 font-medium">
              <div className="w-7 h-7 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0 border border-blue-100">
                <Calculator className="w-4 h-4" />
              </div>
              <span>Coordinate-aware extraction & credit weighting</span>
            </div>
            <div className="flex items-center space-x-3 text-xs text-slate-600 font-medium">
              <div className="w-7 h-7 rounded-lg bg-blue-50 text-blue-600 flex items-center justify-center flex-shrink-0 border border-blue-100">
                <ShieldCheck className="w-4 h-4" />
              </div>
              <span>Authenticated semester records & cumulative CGPA</span>
            </div>
          </div>
        </div>
      </div>

      <p className="mt-6 text-xs text-slate-400 font-medium z-10 text-center">
        Rajalakshmi Institute of Technology • An Autonomous Institution • Affiliated to Anna University
      </p>
    </div>
  );
};

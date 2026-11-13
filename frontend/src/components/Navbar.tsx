import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Upload,
  History,
  LayoutDashboard,
  LogOut,
  Menu,
  X,
  FileCheck2,
} from 'lucide-react';

export const Navbar: React.FC = () => {
  const { currentUser, logout } = useAuth();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navLinks = [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/upload', label: 'Upload Result', icon: Upload },
    { to: '/history', label: 'Semester History', icon: History },
  ];

  const handleLogout = async () => {
    try {
      await logout();
    } catch (e) {
      console.error('Logout error:', e);
    }
  };

  return (
    <header className="sticky top-0 z-50 bg-white/95 backdrop-blur-md border-b border-slate-200 shadow-sm transition-all font-poppins">
      {/* Top subtle blue accent strip */}
      <div className="h-1 bg-gradient-to-r from-blue-700 via-sky-500 to-blue-600" />

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-20 py-2">
          {/* Official RIT Logo & Brand */}
          <Link to="/" className="flex items-center space-x-3.5 group">
            <div className="bg-white p-1.5 rounded-xl border border-slate-200 shadow-sm flex items-center justify-center group-hover:shadow-md transition-all">
              <img
                src="/rit-logo.png"
                alt="Rajalakshmi Institute of Technology Logo"
                className="h-11 sm:h-12 w-auto object-contain"
              />
            </div>
            <div className="hidden sm:block">
              <div className="flex items-center space-x-2">
                <span className="font-extrabold text-lg text-slate-900 tracking-tight flex items-center gap-1.5 font-poppins">
                  RIT <span className="text-blue-600">GPA</span> Calculator
                </span>
                <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold bg-blue-50 text-blue-700 border border-blue-200">
                  Autonomous
                </span>
              </div>
              <p className="text-[11px] text-slate-500 font-medium tracking-wide">
                Rajalakshmi Institute of Technology • Chennai
              </p>
            </div>
          </Link>

          {/* Desktop Navigation Links (Light Blue & Navy theme) */}
          <nav className="hidden md:flex items-center space-x-1.5 bg-slate-50/90 p-1.5 rounded-2xl border border-slate-200">
            {navLinks.map((link) => {
              const Icon = link.icon;
              const isActive = location.pathname === link.to;
              return (
                <Link
                  key={link.to}
                  to={link.to}
                  className={`flex items-center space-x-2 px-4 py-2 rounded-xl text-xs font-semibold tracking-wide transition-all duration-200 ${
                    isActive
                      ? 'bg-white text-blue-700 shadow-sm font-bold border border-blue-200'
                      : 'text-slate-600 hover:text-blue-900 hover:bg-white/60'
                  }`}
                >
                  <Icon className={`w-4 h-4 ${isActive ? 'text-blue-600' : 'text-slate-400'}`} />
                  <span>{link.label}</span>
                </Link>
              );
            })}
          </nav>

          {/* User profile & Logout */}
          <div className="hidden md:flex items-center space-x-3">
            {currentUser && (
              <div className="flex items-center space-x-3 pl-3">
                <div className="flex items-center space-x-2.5 px-3 py-1.5 rounded-xl bg-blue-50/60 border border-blue-100">
                  {currentUser.photoURL ? (
                    <img
                      src={currentUser.photoURL}
                      alt={currentUser.displayName || 'Student'}
                      className="w-7 h-7 rounded-lg border border-blue-200 object-cover"
                    />
                  ) : (
                    <div className="w-7 h-7 rounded-lg bg-blue-600 text-white font-bold flex items-center justify-center text-xs shadow-sm">
                      {currentUser.displayName ? currentUser.displayName.charAt(0).toUpperCase() : 'S'}
                    </div>
                  )}
                  <div className="text-left">
                    <p className="text-xs font-bold text-slate-800 leading-none">
                      {currentUser.displayName || 'Student'}
                    </p>
                    <p className="text-[10px] text-slate-400 truncate max-w-[140px] mt-0.5">
                      {currentUser.email}
                    </p>
                  </div>
                </div>
                <button
                  onClick={handleLogout}
                  title="Sign Out"
                  className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-xl border border-transparent hover:border-red-100 transition-all duration-200 cursor-pointer"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            )}
          </div>

          {/* Mobile Menu Button */}
          <div className="flex md:hidden items-center space-x-2">
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="p-2 rounded-xl text-slate-600 hover:bg-slate-100 border border-slate-200"
            >
              {mobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
            </button>
          </div>
        </div>

        {/* Mobile Dropdown */}
        {mobileMenuOpen && (
          <div className="md:hidden py-4 border-t border-slate-100 space-y-2 animate-fadeIn">
            {navLinks.map((link) => {
              const Icon = link.icon;
              const isActive = location.pathname === link.to;
              return (
                <Link
                  key={link.to}
                  to={link.to}
                  onClick={() => setMobileMenuOpen(false)}
                  className={`flex items-center space-x-3 px-4 py-2.5 rounded-xl text-xs font-semibold ${
                    isActive
                      ? 'bg-blue-50 text-blue-700 font-bold border border-blue-200'
                      : 'text-slate-600 hover:bg-slate-50'
                  }`}
                >
                  <Icon className="w-4 h-4 text-blue-600" />
                  <span>{link.label}</span>
                </Link>
              );
            })}
            {currentUser && (
              <div className="pt-3 border-t border-slate-100 flex items-center justify-between px-2">
                <div className="flex items-center space-x-2">
                  <div className="w-7 h-7 rounded-lg bg-blue-600 text-white font-bold flex items-center justify-center text-xs">
                    {currentUser.displayName ? currentUser.displayName.charAt(0).toUpperCase() : 'S'}
                  </div>
                  <div>
                    <p className="text-xs font-bold text-slate-800">{currentUser.displayName}</p>
                    <p className="text-[10px] text-slate-400">{currentUser.email}</p>
                  </div>
                </div>
                <button
                  onClick={handleLogout}
                  className="flex items-center space-x-1 text-xs font-semibold text-red-600 bg-red-50 px-3 py-1.5 rounded-lg border border-red-100 cursor-pointer"
                >
                  <LogOut className="w-3.5 h-3.5" />
                  <span>Logout</span>
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </header>
  );
};

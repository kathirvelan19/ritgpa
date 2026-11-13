import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';

const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "AIzaSyCDBCRbJKJ6KdNbQW8wvqNK0XZ1nuDvhBY",
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "ritgpacalc.firebaseapp.com",
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "ritgpacalc",
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "ritgpacalc.firebasestorage.app",
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "206359036041",
  appId: import.meta.env.VITE_FIREBASE_APP_ID || "1:206359036041:web:fda218ce1e3fb9498fc658",
  measurementId: import.meta.env.VITE_FIREBASE_MEASUREMENT_ID || "G-CVKYXJXVTG"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();

export default app;

import { initializeApp } from 'firebase/app';
import { getAuth, GoogleAuthProvider } from 'firebase/auth';

const firebaseConfig = {
  apiKey: "AIzaSyCDBCRbJKJ6KdNbQW8wvqNK0XZ1nuDvhBY",
  authDomain: "ritgpacalc.firebaseapp.com",
  projectId: "ritgpacalc",
  storageBucket: "ritgpacalc.firebasestorage.app",
  messagingSenderId: "206359036041",
  appId: "1:206359036041:web:fda218ce1e3fb9498fc658",
  measurementId: "G-CVKYXJXVTG"
};

const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);
export const googleProvider = new GoogleAuthProvider();

export default app;


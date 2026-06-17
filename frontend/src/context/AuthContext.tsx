import React, { createContext, useContext, useState } from 'react';

interface User { id: string; email: string; role: string; name: string; }
interface AuthCtx { user: User | null; login: (e: string, p: string) => string | null; logout: () => void; error: string; setError: (s: string) => void; }

const AuthContext = createContext<AuthCtx>({} as AuthCtx);

const MOCK_USERS: User[] = [
  { id:'1', email:'client@bank.com',    role:'CLIENT',    name:'Jean Dupont' },
  { id:'2', email:'admin@bank.com',     role:'ADMIN',     name:'Admin Système' },
  { id:'3', email:'operateur@bank.com', role:'OPERATEUR', name:'MTN Mobile Money' },
];
const PASSWORDS: Record<string, string> = { 'client@bank.com':'1234', 'admin@bank.com':'1234', 'operateur@bank.com':'1234' };

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [error, setError] = useState('');

  const login = (email: string, password: string) => {
    const found = MOCK_USERS.find(u => u.email === email);
    if (found && PASSWORDS[email] === password) { setUser(found); setError(''); return found.role; }
    setError('Email ou mot de passe incorrect');
    return null;
  };

  const logout = () => setUser(null);

  return <AuthContext.Provider value={{ user, login, logout, error, setError }}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);

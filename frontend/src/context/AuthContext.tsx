import React, { createContext, useContext, useState, useEffect } from 'react';
import { authApi } from '../api/services';

interface User {
  id: string;
  email: string;
  role: string;
  name: string;
}

interface AuthCtx {
  user: User | null;
  loading: boolean;
  error: string;
  setError: (s: string) => void;
  login: (email: string, password: string) => Promise<string | null>;
  logout: () => void;
}

const AuthContext = createContext<AuthCtx>({} as AuthCtx);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Restaurer la session depuis localStorage au démarrage
  useEffect(() => {
    const saved = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    if (saved && token) {
      setUser(JSON.parse(saved));
    }
    setLoading(false);
  }, []);

  const login = async (email: string, password: string): Promise<string | null> => {
    setError('');
    try {
      const res = await authApi.login(email, password);
      const data = res.data;

      // L'identity-service retourne : { accessToken, refreshToken, userId, email, role, ... }
      const token = data.accessToken || data.token || data.access_token;
      const role  = data.role || data.userRole;
      const userId = data.userId || data.id || data.sub;
      const name  = data.nom
        ? `${data.nom} ${data.prenom ?? ''}`.trim()
        : data.name || email.split('@')[0];

      const loggedUser: User = { id: userId, email, role, name };

      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify(loggedUser));
      setUser(loggedUser);
      return role;
    } catch (err: any) {
      const msg = err.response?.data?.message
        || err.response?.data?.error
        || 'Email ou mot de passe incorrect';
      setError(msg);
      return null;
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, error, setError, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);

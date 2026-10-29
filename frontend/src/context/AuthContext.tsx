import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, Role } from '../api/types';
import { authApi } from '../api/apiService';

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
  isStaff: boolean;
  isAdmin: boolean;
  isManager: boolean;
  isRequester: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('sentineldesk_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [token, setToken] = useState<string | null>(() => {
    return localStorage.getItem('sentineldesk_token');
  });
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    async function loadUser() {
      if (token) {
        try {
          const profile = await authApi.getCurrentUser();
          setUser(profile);
          localStorage.setItem('sentineldesk_user', JSON.stringify(profile));
        } catch (err) {
          console.error('Failed to restore session:', err);
          logout();
        }
      }
      setIsLoading(false);
    }
    loadUser();
  }, [token]);

  const login = async (email: string, password: string) => {
    const res = await authApi.login({ email, password });
    setToken(res.token);
    setUser(res.user);
    localStorage.setItem('sentineldesk_token', res.token);
    localStorage.setItem('sentineldesk_user', JSON.stringify(res.user));
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('sentineldesk_token');
    localStorage.removeItem('sentineldesk_user');
  };

  const isStaff = user?.role === 'ADMIN' || user?.role === 'SUPPORT_MANAGER' || user?.role === 'SUPPORT_AGENT';
  const isAdmin = user?.role === 'ADMIN';
  const isManager = user?.role === 'ADMIN' || user?.role === 'SUPPORT_MANAGER';
  const isRequester = user?.role === 'REQUESTER';

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        login,
        logout,
        isLoading,
        isStaff,
        isAdmin,
        isManager,
        isRequester,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

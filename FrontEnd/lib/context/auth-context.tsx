'use client';

import React, { createContext, useContext, useEffect, useState } from 'react';
import { User } from '../types';
import { login as apiLogin, register as apiRegister } from '../api';

export type UserRole = 'ADMIN' | 'STAFF' | 'KITCHEN' | 'CUSTOMER';

interface AuthContextType {
  user: User | null;
  login: (username: string, password: string) => Promise<void>;
  register: (username: string, email: string, password: string, fullName?: string, phone?: string) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
  isAuthenticated: boolean;
  role: UserRole | null;
  hasRole: (roles: UserRole | UserRole[]) => boolean;
  hasAnyRole: (roles: UserRole[]) => boolean;
  isAdmin: boolean;
  isStaff: boolean;
  isKitchen: boolean;
  isCustomer: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    // Mark as hydrated after component mounts
    setIsHydrated(true);

    // Check if user is logged in on app start
    if (typeof window !== 'undefined') {
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        try {
          setUser(JSON.parse(storedUser));
        } catch (error) {
          // Silently remove corrupted data instead of logging during hydration
          localStorage.removeItem('user');
        }
      }
    }
    setIsLoading(false);
  }, []);

  const login = async (username: string, password: string) => {
    try {
      setIsLoading(true);
      const response = await apiLogin({ username, password });
      const userData = response.user;
      const token = response.token; // Get token from response
      
      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
      
      // Save JWT token
      if (token) {
        localStorage.setItem('token', token);
        localStorage.setItem('jwt', token); // Also save as 'jwt' for compatibility
      }
    } catch (error) {
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (username: string, email: string, password: string, fullName?: string, phone?: string) => {
    try {
      setIsLoading(true);
      const response = await apiRegister({ 
        username, 
        email, 
        password, 
        fullName: fullName || '', 
        phone: phone || '' 
      });
      const userData = response.user;
      const token = response.token; // Get token from response
      
      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
      
      // Save JWT token
      if (token) {
        localStorage.setItem('token', token);
        localStorage.setItem('jwt', token); // Also save as 'jwt' for compatibility
      }
    } catch (error) {
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    setUser(null);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
    localStorage.removeItem('jwt');
  };

  const role = user?.role as UserRole || null;

  const hasRole = (roles: UserRole | UserRole[]): boolean => {
    if (!role) return false;
    const roleArray = Array.isArray(roles) ? roles : [roles];
    return roleArray.includes(role);
  };

  const hasAnyRole = (roles: UserRole[]): boolean => {
    if (!role) return false;
    return roles.includes(role);
  };

  const value: AuthContextType = {
    user,
    login,
    register,
    logout,
    isLoading,
    isAuthenticated: !!user,
    role,
    hasRole,
    hasAnyRole,
    isAdmin: role === 'ADMIN',
    isStaff: role === 'STAFF',
    isKitchen: role === 'KITCHEN',
    isCustomer: role === 'CUSTOMER',
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}

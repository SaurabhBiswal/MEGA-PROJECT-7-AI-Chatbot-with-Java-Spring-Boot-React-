import React, { createContext, useContext, useState, useEffect } from 'react';
import api from '../services/api';

interface AuthContextType {
    token: string | null;
    username: string | null;
    login: (username: string, password: string) => Promise<void>;
    register: (username: string, password: string) => Promise<void>;
    logout: () => void;
    isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
    const [username, setUsername] = useState<string | null>(localStorage.getItem('username'));

    const login = async (u: string, p: string) => {
        const response = await api.post('/auth/login', { username: u, password: p });
        const { token: newToken, username: user } = response.data; // Assuming backend returns { token, username }

        // If backend just returns string token, we might need to adjust.
        // Assuming Standard JWT response.

        localStorage.setItem('token', newToken);
        localStorage.setItem('username', user);
        setToken(newToken);
        setUsername(user);
    };

    const register = async (u: string, p: string) => {
        await api.post('/auth/register', { username: u, password: p });
        // Auto login after register? or just redirect to login.
        // For smoother UX, let's auto-login
        await login(u, p);
    };

    const logout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        setToken(null);
        setUsername(null);
    };

    return (
        <AuthContext.Provider value={{ token, username, login, register, logout, isAuthenticated: !!token }}>
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

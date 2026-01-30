import React, { createContext, useContext, useEffect, useState } from 'react';

type Theme = 'light' | 'dark' | 'system';

interface ThemeContextType {
    theme: Theme;
    resolvedTheme: 'light' | 'dark';
    setTheme: (theme: Theme) => void;
    toggleTheme: () => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const ThemeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    // 1. Initial State
    const [theme, setTheme] = useState<Theme>(() => {
        if (typeof window !== 'undefined') {
            return (localStorage.getItem('theme') as Theme) || 'system';
        }
        return 'system';
    });

    const [resolvedTheme, setResolvedTheme] = useState<'light' | 'dark'>('light');

    // 2. Resolve Theme (Logic)
    useEffect(() => {
        const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

        const updateResolvedTheme = () => {
            if (theme === 'system') {
                setResolvedTheme(mediaQuery.matches ? 'dark' : 'light');
            } else {
                setResolvedTheme(theme as 'light' | 'dark');
            }
        };

        updateResolvedTheme();
        localStorage.setItem('theme', theme);

        if (theme === 'system') {
            mediaQuery.addEventListener('change', updateResolvedTheme);
            return () => mediaQuery.removeEventListener('change', updateResolvedTheme);
        }
    }, [theme]);

    // 3. APPLY TO DOM (Crucial step)
    useEffect(() => {
        const root = window.document.documentElement;

        // Remove old classes and add new one
        root.classList.remove('light', 'dark');
        root.classList.add(resolvedTheme);

        // Ensure body also has it for certain CSS selectors
        document.body.classList.remove('light', 'dark');
        document.body.classList.add(resolvedTheme);

        console.log(`[Theme] DOM Updated: ${resolvedTheme} | HTML Class: ${root.className}`);
    }, [resolvedTheme]);

    const toggleTheme = () => {
        setTheme(current => {
            if (current === 'light') return 'dark';
            if (current === 'dark') return 'system';
            return 'light';
        });
    };

    return (
        <ThemeContext.Provider value={{ theme, resolvedTheme, setTheme, toggleTheme }}>
            {children}
        </ThemeContext.Provider>
    );
};

export const useTheme = () => {
    const context = useContext(ThemeContext);
    if (!context) throw new Error('useTheme must be used within a ThemeProvider');
    return context;
};

import React from 'react';
import { Sun, Moon, Monitor } from 'lucide-react';
import { useTheme } from '../context/ThemeContext';

const ThemeToggle: React.FC = () => {
    const { theme, setTheme, resolvedTheme } = useTheme();

    // ✅ Debug logs
    React.useEffect(() => {
        console.log('ThemeToggle Debug:', {
            theme,
            resolvedTheme,
            htmlClass: document.documentElement.className,
            localStorage: localStorage.getItem('theme')
        });
    }, [theme, resolvedTheme]);

    const handleThemeChange = (newTheme: 'light' | 'dark' | 'system') => {
        console.log('Changing theme to:', newTheme);
        setTheme(newTheme);

        // Check after 100ms
        setTimeout(() => {
            console.log('After change:', {
                htmlClass: document.documentElement.className,
                localStorage: localStorage.getItem('theme')
            });
        }, 100);
    };

    return (
        <div className="flex items-center gap-2">
            {/* Current theme indicator */}
            <div className={`text-xs px-2 py-1 rounded font-medium ${resolvedTheme === 'dark'
                    ? 'bg-gray-800 text-gray-300'
                    : 'bg-yellow-100 text-yellow-800'
                }`}>
                {resolvedTheme === 'dark' ? 'DARK' : 'LIGHT'}
            </div>

            <div className="bg-gray-100 dark:bg-gray-800 p-1 rounded-lg flex items-center gap-1 border border-gray-200 dark:border-gray-700">
                <button
                    onClick={() => handleThemeChange('light')}
                    className={`p-2 rounded-md transition-all ${theme === 'light'
                        ? 'bg-white dark:bg-gray-600 text-yellow-600 shadow-lg border border-yellow-300'
                        : 'text-gray-500 hover:bg-gray-200 dark:hover:bg-gray-700 hover:text-yellow-500'
                        }`}
                    title="Light Mode"
                    aria-label="Light theme"
                >
                    <Sun className="h-4 w-4" />
                </button>

                <button
                    onClick={() => handleThemeChange('system')}
                    className={`p-2 rounded-md transition-all ${theme === 'system'
                        ? 'bg-white dark:bg-gray-600 text-green-600 shadow-lg border border-green-300'
                        : 'text-gray-500 hover:bg-gray-200 dark:hover:bg-gray-700 hover:text-green-500'
                        }`}
                    title="System Default"
                    aria-label="System theme"
                >
                    <Monitor className="h-4 w-4" />
                </button>

                <button
                    onClick={() => handleThemeChange('dark')}
                    className={`p-2 rounded-md transition-all ${theme === 'dark'
                        ? 'bg-white dark:bg-gray-600 text-blue-500 shadow-lg border border-blue-300'
                        : 'text-gray-500 hover:bg-gray-200 dark:hover:bg-gray-700 hover:text-blue-400'
                        }`}
                    title="Dark Mode"
                    aria-label="Dark theme"
                >
                    <Moon className="h-4 w-4" />
                </button>
            </div>
        </div>
    );
};

export default ThemeToggle;

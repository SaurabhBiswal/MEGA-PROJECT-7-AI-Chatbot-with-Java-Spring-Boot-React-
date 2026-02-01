import axios from 'axios';

const api = axios.create({
    baseURL: (import.meta.env.VITE_API_URL || 'https://mega-project-7-ai-chatbot-with-java-spring-boot-production.up.railway.app') + '/api',
    headers: {
        'Content-Type': 'application/json',
    },
});

api.interceptors.request.use(
    (config) => {
        const token = sessionStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export default api;

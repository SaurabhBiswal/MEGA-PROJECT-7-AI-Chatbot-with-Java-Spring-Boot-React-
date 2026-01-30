# Saurabh AI 🚀 (Java Spring Boot + React + Groq)

## Overview
**Saurabh AI** is a high-performance, full-stack AI chat application. It's built with a modern tech stack and powered by the lightning-fast **Groq Llama 3.3** model.

## Key Features
- **Fast AI Responses**: Powered by Groq (OpenAI-compatible) using Llama 3.3.
- **Modern UI**: Built with React and **Tailwind CSS v4** with a robust dark/light theme engine.
- **Interactive Chat**: Includes Message Copy, Editing, and dedicated Paste functionality.
- **Real-time**: Leverages Spring WebSockets (STOMP) for seamless communication.
- **Security**: JWT-based authentication and environment variable protection for API keys.

## Tech Stack
### Frontend
- **React 18** + **Vite** + **TypeScript**
- **Tailwind CSS v4** (Global CSS Variables)
- **Lucide Icons** for interactive UI
- **React Markdown** with GFM support

### Backend
- **Spring Boot 3** (Web, Security, Data JPA, WebSocket)
- **Groq API** Integration (OpenAI format)
- **H2 Database** (Local/Dev) / **PostgreSQL** (Prod)
- **JWT Authentication**

## Local Setup

### Prerequisites
- Java 17+
- Node.js 18+

### 1. Backend Configuration
Create/Update `backend/src/main/resources/application.properties`:
```properties
groq.api.key=${GROQ_API_KEY}
groq.api.url=https://api.groq.com/openai/v1/chat/completions
jwt.secret=your_super_secret_key
```

Run Backend:
```bash
# In the root or backend folder
mvn spring-boot:run
```

### 2. Frontend Configuration
Navigate to `frontend/` and run:
```bash
npm install
npm run dev
```

---
Developed with ❤️ by Saurabh

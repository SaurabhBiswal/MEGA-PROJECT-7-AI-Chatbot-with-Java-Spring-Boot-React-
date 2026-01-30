import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import ChatWindow from './components/ChatWindow';
import ThemeToggle from './components/ThemeToggle';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import api from './services/api';

// Protected Route Component
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { token } = useAuth();
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};


const Dashboard = () => {
  const { logout, username } = useAuth();
  const [conversations, setConversations] = useState<any[]>([]);
  const [selectedConvId, setSelectedConvId] = useState<number | undefined>();
  const [renamingId, setRenamingId] = useState<number | null>(null);
  const [renamingTitle, setRenamingTitle] = useState('');

  const fetchConversations = React.useCallback(async () => {
    try {
      const res = await api.get('/conversations');
      if (Array.isArray(res.data)) {
        setConversations(res.data);
        if (res.data.length > 0 && !selectedConvId) {
          setSelectedConvId(res.data[0].id);
        }
      } else {
        console.warn("API did not return an array for conversations:", res.data);
        setConversations([]);
      }
    } catch (err) {
      console.error("Error fetching conversations:", err);
      setConversations([]);
    }
  }, [selectedConvId]);

  useEffect(() => {
    fetchConversations();
  }, [fetchConversations]);

  const handleNewChat = async () => {
    try {
      const res = await api.post('/conversations', "New Chat");
      setConversations([res.data, ...conversations]);
      setSelectedConvId(res.data.id);
    } catch (err) {
      console.error("Error creating new chat:", err);
    }
  };

  const handleRename = async (id: number) => {
    if (!renamingTitle.trim()) {
      setRenamingId(null);
      return;
    }
    try {
      await api.put(`/conversations/${id}`, { title: renamingTitle });
      setRenamingId(null);
      fetchConversations();
    } catch (err) {
      console.error("Error renaming conversation:", err);
    }
  };

  const handleDeleteChat = async (id: number) => {
    if (!window.confirm("Are you sure you want to delete this chat?")) return;

    try {
      await api.delete(`/conversations/${id}`);

      // Update local state
      const updatedConversations = conversations.filter(c => c.id !== id);
      setConversations(updatedConversations);

      // If the deleted chat was selected, select the next available one
      if (selectedConvId === id) {
        setSelectedConvId(updatedConversations.length > 0 ? updatedConversations[0].id : undefined);
      }
    } catch (err) {
      console.error("Error deleting conversation:", err);
    }
  };

  return (
    <div className="flex h-screen w-screen bg-gray-50 dark:bg-black text-gray-900 dark:text-white overflow-hidden font-sans transition-colors duration-200">

      {/* Sidebar (Desktop) */}
      <div className="hidden md:flex w-[260px] flex-col bg-white dark:bg-[#171717] border-r border-gray-200 dark:border-gray-800 p-3 transition-colors duration-200">
        <div
          onClick={handleNewChat}
          className="flex items-center gap-3 px-3 py-4 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg cursor-pointer transition mb-4 border border-gray-200 dark:border-gray-700 shadow-sm"
        >
          <div className="w-8 h-8 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-xs">AI</div>
          <span className="font-semibold text-sm">New Chat</span>
          <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 ml-auto text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
        </div>

        <div className="flex-1 overflow-y-auto space-y-1">
          <div className="text-xs font-semibold text-gray-500 px-3 py-2 uppercase tracking-wider">History</div>
          {conversations.map((conv) => (
            <div
              key={conv.id}
              onClick={() => setSelectedConvId(conv.id)}
              className={`group flex items-center px-3 py-3 text-sm rounded-lg cursor-pointer transition ${selectedConvId === conv.id
                ? 'bg-gray-200 dark:bg-gray-800 text-black dark:text-white font-medium'
                : 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 hover:text-gray-900 dark:hover:text-gray-200'
                }`}
            >
              {renamingId === conv.id ? (
                <div className="flex items-center gap-1 flex-1 min-w-0" onClick={(e) => e.stopPropagation()}>
                  <input
                    autoFocus
                    className="flex-1 bg-white dark:bg-gray-700 border border-blue-500 rounded px-1 py-0.5 outline-none text-xs"
                    value={renamingTitle}
                    onChange={(e) => setRenamingTitle(e.target.value)}
                    onKeyDown={(e) => {
                      if (e.key === 'Enter') handleRename(conv.id);
                      if (e.key === 'Escape') setRenamingId(null);
                    }}
                  />
                  <button
                    onClick={() => handleRename(conv.id)}
                    className="p-1 text-green-500 hover:bg-green-100 dark:hover:bg-green-900/30 rounded"
                    title="Save"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                  </button>
                  <button
                    onClick={() => setRenamingId(null)}
                    className="p-1 text-red-500 hover:bg-red-100 dark:hover:bg-red-900/30 rounded"
                    title="Cancel"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              ) : (
                <>
                  <span className="flex-1 truncate">{conv.title || "Untitled Chat"}</span>
                  <div className="flex items-center opacity-0 group-hover:opacity-100 transition-opacity">
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        setRenamingId(conv.id);
                        setRenamingTitle(conv.title || '');
                      }}
                      className="p-1 hover:text-blue-500 transition-colors"
                      title="Rename"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                      </svg>
                    </button>
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeleteChat(conv.id);
                      }}
                      className="p-1 hover:text-red-500 transition-colors"
                      title="Delete"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-3.5 w-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                      </svg>
                    </button>
                  </div>
                </>
              )}
            </div>
          ))}
          {conversations.length === 0 && (
            <div className="px-3 py-2 text-xs text-gray-500 italic">No previous chats</div>
          )}
        </div>

        <div className="border-t border-gray-200 dark:border-gray-800 pt-3 mt-3 space-y-2">
          <ThemeToggle />

          <div className="flex items-center gap-3 px-3 py-3 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg cursor-pointer transition group border border-transparent hover:border-gray-200 dark:hover:border-gray-700">
            <div className="w-8 h-8 bg-blue-600 rounded-md flex items-center justify-center text-white font-bold flex-shrink-0">
              {username?.charAt(0).toUpperCase() || '?'}
            </div>
            <div className="flex flex-col flex-1 min-w-0">
              <span className="text-sm font-medium text-gray-900 dark:text-white truncate" title={username || 'User'}>
                {username}
              </span>
            </div>
            <button
              onClick={(e) => {
                e.stopPropagation();
                logout();
              }}
              className="ml-auto text-gray-400 hover:text-red-500 transition p-1 rounded-md hover:bg-gray-200 dark:hover:bg-gray-700 flex-shrink-0"
              title="Logout"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col h-full bg-gray-50 dark:bg-[#121212] transition-colors duration-200">
        <ChatWindow
          conversationId={selectedConvId}
          onConversationUpdate={fetchConversations}
        />
      </div>
    </div>
  );
};

const App = () => {
  return (
    <Router>
      <ThemeProvider>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/dashboard" element={
              <ProtectedRoute>
                <Dashboard />
              </ProtectedRoute>
            } />
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </AuthProvider>
      </ThemeProvider>
    </Router>
  );
};

export default App;

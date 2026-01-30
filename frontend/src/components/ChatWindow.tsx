import React, { useState, useRef, useEffect } from 'react';
import { useChat } from '../hooks/useChat';
import ChatMessage from './ChatMessage';
import { useAuth } from '../context/AuthContext';
import { ClipboardPaste } from 'lucide-react';

interface ChatWindowProps {
    conversationId?: number;
    onConversationUpdate?: () => void;
}

const ChatWindow: React.FC<ChatWindowProps> = ({ conversationId, onConversationUpdate }) => {
    const [input, setInput] = useState('');
    const { messages, sendMessage, isConnected } = useChat(conversationId, onConversationUpdate);
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const textareaRef = useRef<HTMLTextAreaElement>(null);
    const { username } = useAuth();

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    // Auto-resize textarea
    useEffect(() => {
        if (textareaRef.current) {
            textareaRef.current.style.height = 'auto';
            const newHeight = Math.min(textareaRef.current.scrollHeight, 160); // 160px is approx max-h-40
            textareaRef.current.style.height = `${newHeight}px`;
        }
    }, [input]);

    const handleSend = (e: React.FormEvent) => {
        e.preventDefault();
        if (input.trim()) {
            sendMessage(input);
            setInput('');
            // Reset height
            if (textareaRef.current) {
                textareaRef.current.style.height = 'auto';
            }
        }
    };

    const handleEdit = (content: string) => {
        if (!content) return; // 🛡️ Safety
        setInput(content);
        if (textareaRef.current) {
            textareaRef.current.focus();
            // Move cursor to end
            const length = content.length;
            textareaRef.current.setSelectionRange(length, length);
        }
    };

    const handlePaste = async () => {
        try {
            const text = await navigator.clipboard.readText();
            if (text) {
                setInput(prev => (prev || '') + text);
                textareaRef.current?.focus();
            }
        } catch (err) {
            console.error('Failed to read clipboard', err);
        }
    };

    return (
        <div className="flex flex-col h-full w-full max-w-4xl mx-auto px-4">
            {/* Connection Status */}
            {!isConnected && (
                <div className="bg-red-900/30 text-red-500 dark:text-red-400 text-xs py-1 px-3 rounded-full mx-auto mt-4 border border-red-800 animate-pulse">
                    Reconnecting to server...
                </div>
            )}

            {/* Chat History */}
            <div className="flex-1 overflow-y-auto pt-8 pb-4 space-y-6 scrollbar-thin scrollbar-thumb-gray-300 dark:scrollbar-thumb-gray-800 scrollbar-track-transparent">
                {messages.length === 0 ? (
                    <div className="h-full flex flex-col items-center justify-center text-gray-500 space-y-4">
                        <div className="w-16 h-16 bg-gray-200 dark:bg-[#2f2f2f] rounded-2xl flex items-center justify-center text-3xl">🤖</div>
                        <h2 className="text-xl font-medium text-gray-700 dark:text-gray-300">How can I help you today?</h2>
                        <p className="text-sm max-w-xs text-center opacity-70">
                            Start a conversation by typing a message below.
                        </p>
                    </div>
                ) : (
                    messages.map((msg, idx) => (
                        <ChatMessage
                            key={idx}
                            content={msg.content}
                            sender={msg.sender}
                            isUser={msg.sender === username}
                            onEdit={handleEdit}
                        />
                    ))
                )}
                <div ref={messagesEndRef} />
            </div>

            {/* Input Area */}
            <div className="pb-8 pt-2">
                <form
                    onSubmit={handleSend}
                    className="relative group bg-white dark:bg-[#2f2f2f] rounded-2xl border border-gray-200 dark:border-gray-700 focus-within:border-blue-500/50 transition-all shadow-lg dark:shadow-xl"
                >
                    <textarea
                        ref={textareaRef}
                        value={input}
                        onChange={(e) => setInput(e.target.value)}
                        onKeyDown={(e) => {
                            if (e.key === 'Enter' && !e.shiftKey) {
                                e.preventDefault();
                                handleSend(e);
                            }
                        }}
                        placeholder="Message Saurabh AI..."
                        className="w-full bg-transparent text-gray-900 dark:text-white px-4 py-4 pr-12 focus:outline-none resize-none min-h-[56px] max-h-40"
                        rows={1}
                    />
                    <div className="absolute right-2 bottom-2 flex items-center gap-1">
                        <button
                            type="button"
                            onClick={handlePaste}
                            className="p-2 rounded-xl text-gray-400 dark:text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-800 transition-all"
                            title="Paste from clipboard"
                        >
                            <ClipboardPaste size={18} />
                        </button>
                        <button
                            type="submit"
                            disabled={!(input || '').trim() || !isConnected}
                            className={`p-2 rounded-xl transition-all ${(input || '').trim() && isConnected
                                ? 'bg-blue-600 text-white hover:bg-blue-500'
                                : 'bg-gray-200 dark:bg-gray-700 text-gray-400 dark:text-gray-500 cursor-not-allowed'
                                }`}
                        >
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                                <path d="M10.894 2.553a1 1 0 00-1.788 0l-7 14a1 1 0 001.169 1.409l5-1.429A1 1 0 009 15.571V11a1 1 0 112 0v4.571a1 1 0 00.725.962l5 1.428a1 1 0 001.17-1.408l-7-14z" />
                            </svg>
                        </button>
                    </div>
                </form>
                <div className="mt-2 text-[10px] text-center text-gray-500">
                    Saurabh AI can make mistakes. Verify important information.
                </div>
            </div>
        </div>
    );
};

export default ChatWindow;

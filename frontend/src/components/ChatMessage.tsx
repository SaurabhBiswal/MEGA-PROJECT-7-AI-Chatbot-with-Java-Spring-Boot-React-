import React, { useState } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Copy, Check, Edit2 } from 'lucide-react';

interface MessageProps {
    sender: string;
    content: string;
    isUser: boolean;
    timestamp?: string;
    onEdit?: (content: string) => void;
}

const ChatMessage: React.FC<MessageProps> = ({ sender, content, isUser, timestamp, onEdit }) => {
    const [copied, setCopied] = useState(false);

    const handleCopy = () => {
        navigator.clipboard.writeText(content);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <div className={`flex w-full mb-6 ${isUser ? 'justify-end' : 'justify-start'}`}>
            <div className={`flex max-w-[80%] md:max-w-[70%] ${isUser ? 'flex-row-reverse' : 'flex-row'} gap-3 relative group`}>
                {/* Avatar */}
                <div className={`flex-shrink-0 w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold shadow-md
                    ${isUser ? 'bg-blue-600 text-white' : 'bg-green-600 text-white'}`}>
                    {sender.charAt(0).toUpperCase()}
                </div>

                {/* Message Bubble container */}
                <div className="flex flex-col relative w-full">
                    {/* Action Buttons (Hover) */}
                    <div className={`absolute -top-8 ${isUser ? 'left-0' : 'right-0'} flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity`}>
                        <button
                            onClick={handleCopy}
                            className="p-1.5 rounded-md bg-gray-100 dark:bg-gray-800 text-gray-500 hover:text-blue-500 transition-colors border border-gray-200 dark:border-gray-700 shadow-sm"
                            title="Copy message"
                        >
                            {copied ? <Check size={14} className="text-green-500" /> : <Copy size={14} />}
                        </button>
                        {isUser && onEdit && (
                            <button
                                onClick={() => onEdit(content)}
                                className="p-1.5 rounded-md bg-gray-100 dark:bg-gray-800 text-gray-500 hover:text-blue-500 transition-colors border border-gray-200 dark:border-gray-700 shadow-sm"
                                title="Edit message"
                            >
                                <Edit2 size={14} />
                            </button>
                        )}
                    </div>

                    <div className={`px-5 py-3 rounded-2xl shadow-sm text-sm leading-relaxed overflow-hidden
                        ${isUser
                            ? 'bg-blue-600 text-white rounded-tr-sm'
                            : 'bg-white dark:bg-gray-800 text-gray-900 dark:text-gray-100 rounded-tl-sm border border-gray-200 dark:border-gray-700'
                        }`}>

                        {/* Content */}
                        <div className={`prose prose-sm max-w-none break-words ${isUser ? 'prose-invert' : 'dark:prose-invert'}`}>
                            {isUser ? (
                                <p className="whitespace-pre-wrap">{content}</p>
                            ) : (
                                <ReactMarkdown remarkPlugins={[remarkGfm]}>
                                    {content}
                                </ReactMarkdown>
                            )}
                        </div>

                        {/* Timestamp */}
                        {timestamp && (
                            <div className={`text-[10px] opacity-50 mt-1 ${isUser ? 'text-right' : 'text-left'}`}>
                                {new Date(timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ChatMessage;

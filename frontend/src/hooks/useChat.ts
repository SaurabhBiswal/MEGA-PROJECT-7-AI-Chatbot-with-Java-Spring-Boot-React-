import { useEffect, useState, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export interface ChatMessage {
    content: string;
    sender: string;
    conversationId?: number;
    type: 'CHAT' | 'JOIN' | 'LEAVE' | 'CONVERSATION_UPDATE' | 'TYPING' | 'ERROR';
    status?: 'SENT' | 'PROCESSING' | 'RECEIVED' | 'ERROR';
    attachmentUrl?: string;
    attachmentType?: string;
}

export const useChat = (conversationId?: number, onConversationUpdate?: () => void) => {
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [isConnected, setIsConnected] = useState(false);
    const [isTyping, setIsTyping] = useState(false);
    const stompClientRef = useRef<Client | null>(null);
    const { username } = useAuth();

    // Reset and fetch history when conversationId changes
    useEffect(() => {
        if (conversationId) {
            api.get(`/conversations/${conversationId}/messages`)
                .then(res => {
                    const history = res.data.map((m: any) => ({
                        content: m.content,
                        sender: m.sender ? m.sender.username : 'AI Assistant',
                        type: 'CHAT',
                        conversationId: conversationId,
                        status: m.status || 'SENT',
                        attachmentUrl: m.attachmentUrl,
                        attachmentType: m.attachmentType
                    }));
                    setMessages(history);
                })
                .catch(err => console.error("Error fetching history:", err));
        } else {
            setMessages([]);
        }
    }, [conversationId]);

    useEffect(() => {
        if (!username) return;

        const wsBaseUrl = import.meta.env.VITE_WS_URL || 'https://mega-project-7-ai-chatbot-with-java-spring-boot-production.up.railway.app';
        const socket = new SockJS(`${wsBaseUrl}/ws`);
        const client = new Client({
            webSocketFactory: () => socket,
            onConnect: () => {
                setIsConnected(true);
                console.log('Connected to WebSocket');

                client.subscribe('/topic/public', (message) => {
                    const receivedMessage: ChatMessage = JSON.parse(message.body);

                    if (receivedMessage.type === 'CONVERSATION_UPDATE') {
                        onConversationUpdate?.();
                        return;
                    }

                    if (receivedMessage.type === 'TYPING') {
                        if (receivedMessage.conversationId === conversationId) {
                            setIsTyping(true);
                        }
                        return;
                    }

                    if (receivedMessage.type === 'CHAT' || receivedMessage.type === 'ERROR') {
                        // 🛡️ Safety & Cleanliness
                        // 1. Filter by conversationId
                        if (receivedMessage.conversationId && receivedMessage.conversationId !== conversationId) {
                            return;
                        }

                        // Stop typing indicator when a message arrives
                        setIsTyping(false);

                        if (receivedMessage.content || receivedMessage.attachmentUrl) {
                            setMessages((prev) => [...prev, receivedMessage]);
                        }
                    }
                });

                client.publish({
                    destination: '/app/chat.addUser',
                    body: JSON.stringify({ sender: username, type: 'JOIN' })
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
                setIsTyping(false);
            },
        });

        client.activate();
        stompClientRef.current = client;

        return () => {
            client.deactivate();
        };
    }, [username, conversationId, onConversationUpdate]);

    const sendMessage = useCallback((content: string, attachment?: { url: string, type: string }) => {
        if (stompClientRef.current && stompClientRef.current.connected && isConnected && username) {
            const chatMessage: ChatMessage = {
                sender: username,
                content: content,
                conversationId: conversationId,
                type: 'CHAT',
                status: 'SENT',
                attachmentUrl: attachment?.url,
                attachmentType: attachment?.type
            };

            // Add message locally for optimistic UI
            // But be careful because we'll get it back over WebSocket
            // However, the current logic relies on WebSocket for receiving.

            try {
                stompClientRef.current.publish({
                    destination: '/app/chat.sendMessage',
                    body: JSON.stringify(chatMessage),
                });
            } catch (error) {
                console.error("Failed to send message, socket might be disconnected:", error);
                setIsConnected(false);
            }
        } else {
            console.warn("Cannot send message: WebSocket is not connected.");
        }
    }, [isConnected, username, conversationId]);

    return { messages, sendMessage, isConnected, isTyping };
};

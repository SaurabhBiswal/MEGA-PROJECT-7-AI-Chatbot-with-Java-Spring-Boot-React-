import { useEffect, useState, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

export interface ChatMessage {
    content: string;
    sender: string;
    conversationId?: number;
    type: 'CHAT' | 'JOIN' | 'LEAVE' | 'CONVERSATION_UPDATE';
}

export const useChat = (conversationId?: number, onConversationUpdate?: () => void) => {
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [isConnected, setIsConnected] = useState(false);
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
                        conversationId: conversationId
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

        const socket = new SockJS('http://localhost:8080/ws');
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

                    // 🛡️ Safety & Cleanliness
                    // 1. Only show CHAT messages (ignore JOIN/LEAVE)
                    // 2. Ignore messages with no content
                    if (receivedMessage.type !== 'CHAT' || !receivedMessage.content) {
                        return;
                    }

                    // 3. Filter by conversationId
                    if (receivedMessage.conversationId && receivedMessage.conversationId !== conversationId) {
                        return;
                    }

                    setMessages((prev) => [...prev, receivedMessage]);
                });

                client.publish({
                    destination: '/app/chat.addUser',
                    body: JSON.stringify({ sender: username, type: 'JOIN' })
                });
            },
            onDisconnect: () => {
                setIsConnected(false);
            },
        });

        client.activate();
        stompClientRef.current = client;

        return () => {
            client.deactivate();
        };
    }, [username, conversationId, onConversationUpdate]);

    const sendMessage = useCallback((content: string) => {
        if (stompClientRef.current && stompClientRef.current.connected && isConnected && username) {
            const chatMessage: ChatMessage = {
                sender: username,
                content: content,
                conversationId: conversationId,
                type: 'CHAT',
            };
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

    return { messages, sendMessage, isConnected };
};

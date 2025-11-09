package com.example.chat_websocket;


import com.example.chat_websocket.model.ChatMessage;
import com.example.chat_websocket.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de integración para el sistema de chat WebSocket
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatIntegrationTest {

    @LocalServerPort
    private int port;

    private WebSocketStompClient stompClient;
    private String wsUrl;

    @BeforeEach
    void setUp() {
        wsUrl = "ws://localhost:" + port + "/ws";

        // Configurar cliente STOMP con SockJS
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));
        SockJsClient sockJsClient = new SockJsClient(transports);

        stompClient = new WebSocketStompClient(sockJsClient);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    @DisplayName("Debe establecer conexión WebSocket exitosamente")
    void testWebSocketConnection() throws Exception {
        CompletableFuture<Boolean> connected = new CompletableFuture<>();

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                connected.complete(true);
            }

            @Override
            public void handleException(StompSession session, StompCommand command,
                                        StompHeaders headers, byte[] payload, Throwable exception) {
                connected.completeExceptionally(exception);
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                connected.completeExceptionally(exception);
            }
        };

        stompClient.connect(wsUrl, sessionHandler);

        assertTrue(connected.get(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Debe enviar y recibir mensaje correctamente")
    void testSendAndReceiveMessage() throws Exception {
        CompletableFuture<ChatMessage> messageReceived = new CompletableFuture<>();

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                // Suscribirse al topic
                session.subscribe("/topic/public", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return ChatMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        messageReceived.complete((ChatMessage) payload);
                    }
                });

                // Enviar mensaje de prueba
                ChatMessage testMessage = new ChatMessage();
                testMessage.setType(MessageType.CHAT);
                testMessage.setSender("TestUser");
                testMessage.setContent("Integration Test Message");

                session.send("/app/chat.sendMessage", testMessage);
            }
        };

        stompClient.connect(wsUrl, sessionHandler);

        ChatMessage received = messageReceived.get(10, TimeUnit.SECONDS);

        assertNotNull(received);
        assertEquals("TestUser", received.getSender());
        assertEquals("Integration Test Message", received.getContent());
        assertEquals(MessageType.CHAT, received.getType());
    }

    @Test
    @DisplayName("Debe notificar cuando un usuario se une")
    void testUserJoinNotification() throws Exception {
        CompletableFuture<ChatMessage> joinMessage = new CompletableFuture<>();

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/public", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return ChatMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        ChatMessage msg = (ChatMessage) payload;
                        if (msg.getType() == MessageType.JOIN) {
                            joinMessage.complete(msg);
                        }
                    }
                });

                // Simular ingreso de usuario
                ChatMessage joinMsg = new ChatMessage();
                joinMsg.setType(MessageType.JOIN);
                joinMsg.setSender("NewUser");

                session.send("/app/chat.addUser", joinMsg);
            }
        };

        stompClient.connect(wsUrl, sessionHandler);

        ChatMessage received = joinMessage.get(10, TimeUnit.SECONDS);

        assertNotNull(received);
        assertEquals(MessageType.JOIN, received.getType());
        assertEquals("NewUser", received.getSender());
        assertTrue(received.getContent().contains("NewUser"));
    }

    @Test
    @DisplayName("Debe manejar múltiples clientes simultáneos")
    void testMultipleClients() throws Exception {
        int clientCount = 3;
        List<CompletableFuture<ChatMessage>> futures = new ArrayList<>();

        for (int i = 0; i < clientCount; i++) {
            CompletableFuture<ChatMessage> future = new CompletableFuture<>();
            futures.add(future);

            final String username = "User" + i;

            StompSessionHandler handler = new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    session.subscribe("/topic/public", new StompFrameHandler() {
                        @Override
                        public Type getPayloadType(StompHeaders headers) {
                            return ChatMessage.class;
                        }

                        @Override
                        public void handleFrame(StompHeaders headers, Object payload) {
                            ChatMessage msg = (ChatMessage) payload;
                            if (msg.getType() == MessageType.CHAT &&
                                    msg.getContent().contains("Broadcast")) {
                                future.complete(msg);
                            }
                        }
                    });

                    // Solo el primer cliente envía el mensaje broadcast
                    if (username.equals("User0")) {
                        ChatMessage broadcastMsg = new ChatMessage();
                        broadcastMsg.setType(MessageType.CHAT);
                        broadcastMsg.setSender(username);
                        broadcastMsg.setContent("Broadcast to all clients");

                        session.send("/app/chat.sendMessage", broadcastMsg);
                    }
                }
            };

            stompClient.connect(wsUrl, handler);
        }

        // Verificar que todos los clientes recibieron el mensaje
        for (CompletableFuture<ChatMessage> future : futures) {
            ChatMessage msg = future.get(15, TimeUnit.SECONDS);
            assertNotNull(msg);
            assertTrue(msg.getContent().contains("Broadcast"));
        }
    }

    @Test
    @DisplayName("Debe validar timestamp en mensajes")
    void testMessageTimestamp() throws Exception {
        CompletableFuture<ChatMessage> messageReceived = new CompletableFuture<>();

        StompSessionHandler sessionHandler = new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/public", new StompFrameHandler() {
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return ChatMessage.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        ChatMessage msg = (ChatMessage) payload;
                        if (msg.getType() == MessageType.CHAT) {
                            messageReceived.complete(msg);
                        }
                    }
                });

                ChatMessage testMessage = new ChatMessage();
                testMessage.setType(MessageType.CHAT);
                testMessage.setSender("TestUser");
                testMessage.setContent("Timestamp test");

                session.send("/app/chat.sendMessage", testMessage);
            }
        };

        stompClient.connect(wsUrl, sessionHandler);

        ChatMessage received = messageReceived.get(10, TimeUnit.SECONDS);

        assertNotNull(received.getTimestamp());
        assertTrue(received.getTimestamp().matches("\\d{2}:\\d{2}:\\d{2}"));
    }
}
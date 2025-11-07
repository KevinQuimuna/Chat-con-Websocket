package com.example.chat_websocket.config;


import com.example.chat_websocket.model.ChatMessage;
import com.example.chat_websocket.model.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Listener de eventos WebSocket
 * Maneja las conexiones y desconexiones de usuarios
 *
 * @author Sistema de Chat
 * @version 1.0.0
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Constructor con inyección de dependencias
     *
     * @param messagingTemplate Template para enviar mensajes
     */
    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Maneja el evento cuando un cliente se conecta al WebSocket
     *
     * @param event Evento de conexión
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Nueva conexión WebSocket establecida");
    }

    /**
     * Maneja el evento cuando un cliente se desconecta del WebSocket
     * Notifica a todos los usuarios que alguien ha abandonado el chat
     *
     * @param event Evento de desconexión
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            logger.info("Usuario desconectado: {}", username);

            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setType(MessageType.LEAVE);
            chatMessage.setSender(username);
            chatMessage.setContent(username + " ha abandonado el chat");

            // Envía notificación de desconexión a todos los usuarios
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
        }
    }
}
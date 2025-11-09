package com.example.chat_websocket.controller;


import com.example.chat_websocket.model.ChatMessage;
import com.example.chat_websocket.model.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

/**
 * Controlador que maneja los mensajes WebSocket del sistema de chat
 *
 * @author Sistema de Chat
 * @version 1.0.0
 */
@Controller
@Validated
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    /**
     * Maneja los mensajes de chat enviados por los clientes
     *
     * @param chatMessage Mensaje de chat recibido
     * @return Mensaje de chat para ser enviado a todos los suscriptores
     */
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        // Valida que el contenido no esté vacío
        if (chatMessage.getContent() == null || chatMessage.getContent().trim().isEmpty()) {
            logger.warn("Intento de enviar mensaje vacío por usuario: {}", chatMessage.getSender());
            return null;
        }

        // Sanitiza el contenido del mensaje (previene XSS básico)
        String sanitizedContent = sanitizeMessage(chatMessage.getContent());
        chatMessage.setContent(sanitizedContent);

        logger.info("Mensaje recibido de {}: {}",
                chatMessage.getSender(),
                chatMessage.getContent());

        return chatMessage;
    }

    /**
     * Maneja cuando un usuario se une al chat
     * Registra el nombre de usuario en la sesión WebSocket
     *
     * @param chatMessage Mensaje con información del usuario
     * @param headerAccessor Accessor para acceder a los headers de la sesión
     * @return Mensaje de notificación de ingreso
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(@Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {

        // Valida que el nombre de usuario no esté vacío
        if (chatMessage.getSender() == null || chatMessage.getSender().trim().isEmpty()) {
            logger.warn("Intento de conexión sin nombre de usuario");
            return null;
        }

        // Almacena el nombre de usuario en la sesión WebSocket
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());

        logger.info("Usuario conectado: {}", chatMessage.getSender());

        // Crea mensaje de notificación
        ChatMessage notification = new ChatMessage();
        notification.setType(MessageType.JOIN);
        notification.setSender(chatMessage.getSender());
        notification.setContent(chatMessage.getSender() + " se ha unido al chat");

        return notification;
    }

    /**
     * Sanitiza el contenido del mensaje para prevenir ataques XSS básicos
     *
     * @param message Mensaje original
     * @return Mensaje sanitizado
     */
    private String sanitizeMessage(String message) {
        if (message == null) {
            return "";
        }

        return message
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;")
                .replace("/", "&#x2F;")
                .trim();
    }
}
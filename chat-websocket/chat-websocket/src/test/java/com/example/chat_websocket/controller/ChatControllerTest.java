package com.example.chat_websocket.controller;


import com.example.chat_websocket.model.ChatMessage;
import com.example.chat_websocket.model.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para ChatController
 */
class ChatControllerTest {

    private ChatController chatController;
    private SimpMessageHeaderAccessor headerAccessor;

    @BeforeEach
    void setUp() {
        chatController = new ChatController();

        // Crear header accessor mock
        Map<String, Object> sessionAttributes = new HashMap<>();
        headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(sessionAttributes);
    }

    @Test
    @DisplayName("Debe enviar mensaje válido correctamente")
    void testSendMessage_ValidMessage() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.CHAT);
        message.setSender("TestUser");
        message.setContent("Hello World");

        // Act
        ChatMessage result = chatController.sendMessage(message);

        // Assert
        assertNotNull(result);
        assertEquals("TestUser", result.getSender());
        assertEquals("Hello World", result.getContent());
        assertEquals(MessageType.CHAT, result.getType());
        assertNotNull(result.getTimestamp());
    }

    @Test
    @DisplayName("Debe rechazar mensaje vacío")
    void testSendMessage_EmptyContent() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.CHAT);
        message.setSender("TestUser");
        message.setContent("");

        // Act
        ChatMessage result = chatController.sendMessage(message);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe rechazar mensaje null")
    void testSendMessage_NullContent() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.CHAT);
        message.setSender("TestUser");
        message.setContent(null);

        // Act
        ChatMessage result = chatController.sendMessage(message);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe sanitizar caracteres especiales HTML")
    void testSendMessage_SanitizeHTML() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.CHAT);
        message.setSender("TestUser");
        message.setContent("<script>alert('XSS')</script>");

        // Act
        ChatMessage result = chatController.sendMessage(message);

        // Assert
        assertNotNull(result);
        assertFalse(result.getContent().contains("<script>"));
        assertTrue(result.getContent().contains("&lt;script&gt;"));
    }

    @Test
    @DisplayName("Debe agregar usuario y guardar en sesión")
    void testAddUser_ValidUser() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.JOIN);
        message.setSender("NewUser");

        // Act
        ChatMessage result = chatController.addUser(message, headerAccessor);

        // Assert
        assertNotNull(result);
        assertEquals(MessageType.JOIN, result.getType());
        assertEquals("NewUser", result.getSender());
        assertTrue(result.getContent().contains("NewUser"));
        assertTrue(result.getContent().contains("unido"));

        // Verificar que el username se guardó en la sesión
        assertEquals("NewUser", headerAccessor.getSessionAttributes().get("username"));
    }

    @Test
    @DisplayName("Debe rechazar usuario sin nombre")
    void testAddUser_EmptyUsername() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.JOIN);
        message.setSender("");

        // Act
        ChatMessage result = chatController.addUser(message, headerAccessor);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Debe trimear espacios en blanco del mensaje")
    void testSendMessage_TrimWhitespace() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.CHAT);
        message.setSender("TestUser");
        message.setContent("  Hello World  ");

        // Act
        ChatMessage result = chatController.sendMessage(message);

        // Assert
        assertNotNull(result);
        assertEquals("Hello World", result.getContent());
    }

    @Test
    @DisplayName("Debe escapar comillas en el mensaje")
    void testSendMessage_EscapeQuotes() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.CHAT);
        message.setSender("TestUser");
        message.setContent("He said \"Hello\"");

        // Act
        ChatMessage result = chatController.sendMessage(message);

        // Assert
        assertNotNull(result);
        assertTrue(result.getContent().contains("&quot;"));
    }

    @Test
    @DisplayName("Debe procesar mensajes con caracteres Unicode")
    void testSendMessage_UnicodeCharacters() {
        // Arrange
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.CHAT);
        message.setSender("TestUser");
        message.setContent("Hola 👋 ¿Cómo estás? 😊");

        // Act
        ChatMessage result = chatController.sendMessage(message);

        // Assert
        assertNotNull(result);
        assertEquals("Hola 👋 ¿Cómo estás? 😊", result.getContent());
    }
}
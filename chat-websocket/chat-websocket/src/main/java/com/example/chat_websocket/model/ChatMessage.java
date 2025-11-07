package com.example.chat_websocket.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clase que representa un mensaje en el sistema de chat
 *
 * @author Sistema de Chat
 * @version 1.0.0
 */
public class ChatMessage {

    @NotNull(message = "El tipo de mensaje no puede ser nulo")
    private MessageType type;

    @NotBlank(message = "El contenido del mensaje no puede estar vacío")
    private String content;

    @NotBlank(message = "El nombre del remitente no puede estar vacío")
    private String sender;

    private String timestamp;

    /**
     * Constructor por defecto
     */
    public ChatMessage() {
        this.timestamp = generateTimestamp();
    }

    /**
     * Constructor con parámetros
     */
    public ChatMessage(MessageType type, String content, String sender) {
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.timestamp = generateTimestamp();
    }

    /**
     * Genera la marca de tiempo actual en formato legible
     */
    private String generateTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    // Getters y Setters

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
        if (this.timestamp == null) {
            this.timestamp = generateTimestamp();
        }
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", sender='" + sender + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
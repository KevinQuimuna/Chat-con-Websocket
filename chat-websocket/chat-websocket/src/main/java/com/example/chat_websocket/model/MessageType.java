package com.example.chat_websocket.model;

/**
 * Enumeración que define los tipos de mensajes en el sistema de chat
 *
 * @author Sistema de Chat
 * @version 1.0.0
 */
public enum MessageType {
    /**
     * Mensaje de chat normal entre usuarios
     */
    CHAT,

    /**
     * Notificación de que un usuario se ha unido al chat
     */
    JOIN,

    /**
     * Notificación de que un usuario ha abandonado el chat
     */
    LEAVE
}

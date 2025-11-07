package com.example.chat_websocket.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuración de WebSocket para el sistema de chat
 * Habilita STOMP sobre WebSocket para comunicación bidireccional
 *
 * @author Sistema de Chat
 * @version 1.0.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Configura el broker de mensajes
     * Define los prefijos para los destinos de mensajes
     *
     * @param config Registro del broker de mensajes
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Habilita un broker simple en memoria
        // Los mensajes con destino que comience con "/topic" serán enrutados al broker
        config.enableSimpleBroker("/topic");

        // Los mensajes desde el cliente con destino que comience con "/app"
        // serán enrutados a los métodos @MessageMapping en los controladores
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * Registra los endpoints STOMP
     * Define los puntos de conexión WebSocket
     *
     * @param registry Registro de endpoints STOMP
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registra el endpoint "/ws" para conexiones WebSocket
        // withSockJS() proporciona fallback options para navegadores que no soportan WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
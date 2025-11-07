package com.example.chat_websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación de chat con WebSocket
 *
 * @author Sistema de Chat
 * @version 1.0.0
 */
@SpringBootApplication
public class ChatWebsocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatWebsocketApplication.class, args);
		System.out.println("=================================================");
		System.out.println("Sistema de Chat WebSocket iniciado correctamente");
		System.out.println("Accede a: http://localhost:8087");
		System.out.println("=================================================");
	}
}
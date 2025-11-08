'use strict';

// Variables globales
let stompClient = null;
let username = null;

// Elementos del DOM
const usernamePage = document.querySelector('#username-page');
const chatPage = document.querySelector('#chat-page');
const usernameForm = document.querySelector('#usernameForm');
const messageForm = document.querySelector('#messageForm');
const messageInput = document.querySelector('#message');
const messageArea = document.querySelector('#messageArea');
const connectingElement = document.querySelector('.connecting');
const connectedUserElement = document.querySelector('#connected-user');
const logoutBtn = document.querySelector('#logout-btn');

// Colores para diferentes usuarios
const colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

/**
 * Conecta al servidor WebSocket
 */
function connect(event) {
    event.preventDefault();

    username = document.querySelector('#name').value.trim();

    if (!username) {
        alert('Por favor ingresa un nombre de usuario');
        return;
    }

    if (username.length < 2) {
        alert('El nombre debe tener al menos 2 caracteres');
        return;
    }

    if (username.length > 20) {
        alert('El nombre no puede tener más de 20 caracteres');
        return;
    }

    // Oculta página de usuario y muestra página de chat
    usernamePage.classList.add('hidden');
    chatPage.classList.remove('hidden');

    // Actualiza el nombre del usuario conectado
    connectedUserElement.textContent = username;

    // Crea conexión WebSocket usando SockJS
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    // Desactiva logs de debug
    stompClient.debug = null;

    // Conecta al servidor
    stompClient.connect({}, onConnected, onError);
}

/**
 * Callback cuando la conexión es exitosa
 */
function onConnected() {
    // Suscribe al canal público
    stompClient.subscribe('/topic/public', onMessageReceived);

    // Notifica al servidor que el usuario se ha unido
    stompClient.send("/app/chat.addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    );

    // Oculta el spinner de conexión
    connectingElement.classList.add('hidden');

    console.log('Conectado al servidor WebSocket');
}

/**
 * Callback cuando hay un error de conexión
 */
function onError(error) {
    connectingElement.textContent = 'No se pudo conectar al servidor. Intenta recargar la página.';
    connectingElement.style.color = '#d32f2f';
    console.error('Error de conexión:', error);
}

/**
 * Envía un mensaje al servidor
 */
function sendMessage(event) {
    event.preventDefault();

    const messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        const chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT'
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
    }
}

/**
 * Maneja los mensajes recibidos del servidor
 */
function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);

    const messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        const contentElement = document.createElement('div');
        contentElement.classList.add('message-content');
        contentElement.textContent = message.content;
        messageElement.appendChild(contentElement);
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        const contentElement = document.createElement('div');
        contentElement.classList.add('message-content');
        contentElement.textContent = message.content;
        messageElement.appendChild(contentElement);
    } else {
        messageElement.classList.add('chat-message');

        const messageContentDiv = document.createElement('div');
        messageContentDiv.classList.add('message-content');

        const senderElement = document.createElement('div');
        senderElement.classList.add('message-sender');
        senderElement.textContent = message.sender;
        senderElement.style.color = getAvatarColor(message.sender);

        const textElement = document.createElement('div');
        textElement.classList.add('message-text');
        textElement.textContent = message.content;

        const timeElement = document.createElement('div');
        timeElement.classList.add('message-time');
        timeElement.textContent = message.timestamp;

        messageContentDiv.appendChild(senderElement);
        messageContentDiv.appendChild(textElement);
        messageContentDiv.appendChild(timeElement);

        messageElement.appendChild(messageContentDiv);
    }

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

/**
 * Genera un color consistente para cada usuario
 */
function getAvatarColor(messageSender) {
    let hash = 0;
    for (let i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    const index = Math.abs(hash % colors.length);
    return colors[index];
}

/**
 * Desconecta del servidor y cierra la sesión
 */
function logout() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }

    // Reinicia variables
    username = null;
    stompClient = null;

    // Limpia el área de mensajes
    messageArea.innerHTML = '';

    // Muestra la página de usuario
    chatPage.classList.add('hidden');
    usernamePage.classList.remove('hidden');
    connectingElement.classList.remove('hidden');
    connectingElement.textContent = 'Conectando...';
    connectingElement.style.color = '#666';

    // Limpia el campo de nombre
    document.querySelector('#name').value = '';

    console.log('Sesión cerrada');
}

// Event Listeners
usernameForm.addEventListener('submit', connect);
messageForm.addEventListener('submit', sendMessage);
logoutBtn.addEventListener('click', logout);

// Previene que el usuario cierre la página sin desconectarse
window.addEventListener('beforeunload', function(e) {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
});
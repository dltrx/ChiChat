'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var startChattingButton = document.querySelector('#startChattingButton');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
var messageArea = document.querySelector('#messageArea');
var connectingElement = document.querySelector('.connecting');
var typingElement = document.querySelector('.typing');
var usernameElement = document.querySelector('#username');

var stompClient = null;
var username = null;
var typing = false;
var lastTypingTime;
var TYPING_TIMER_LENGTH = 3000; // 3 seconds

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

function connect(event) {
    username = usernameElement.textContent.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }

    event.preventDefault();
}

function onConnected() {
    stompClient.subscribe('/topic/chat', onMessageReceived);
    stompClient.subscribe('/topic/typing', onTypingReceived);

    stompClient.send("/app/chat.addUser", {}, JSON.stringify({ sender: username, type: 'JOIN' }));

    connectingElement.classList.add('hidden');
}

function onError(error) {
    connectingElement.textContent = 'Not able to connect. Please try again';
    connectingElement.style.color = 'red';
}

function sendMessage(event) {
    var messageContent = messageInput.value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageInput.value,
            type: 'CHAT'
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        messageInput.value = '';
        stopTyping();
    }

    event.preventDefault();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

function sendTypingStatus() {
    if (!typing) {
        typing = true;
        stompClient.send("/app/chat.typing", {}, JSON.stringify({ sender: username, typing: true }));
    }
    lastTypingTime = (new Date()).getTime();

    setTimeout(() => {
        var timeDiff = (new Date()).getTime() - lastTypingTime;
        if (timeDiff >= TYPING_TIMER_LENGTH && typing) {
            stopTyping();
        }
    }, TYPING_TIMER_LENGTH);
}

function stopTyping() {
    typing = false;
    stompClient.send("/app/chat.typing", {}, JSON.stringify({ sender: username, typing: false }));
}

function onTypingReceived(payload) {
    var typingUsers = JSON.parse(payload.body);
    var typingArray = [];

    Object.keys(typingUsers).forEach(function(user) {
        if (typingUsers[user] && user !== username) {
            typingArray.push(user);
        }
    });

    if (typingArray.length > 0) {
        var typingText = typingArray.join(', ') + (typingArray.length > 1 ? ' are typing...' : ' is typing...');
        typingElement.textContent = typingText;
        typingElement.style.display = 'block';
    } else {
        typingElement.style.display = 'none';
    }
}

function handleFileUpload(event) {
    var file = event.target.files[0];
    if (file && stompClient) {
        var reader = new FileReader();
        reader.onload = function (e) {
            var fileMessage = {
                sender: username,
                content: e.target.result,
                type: 'FILE',
                fileName: file.name
            };
            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(fileMessage));
        };
        reader.readAsDataURL(file);
    }
}

startChattingButton.addEventListener('click', connect, true);
messageForm.addEventListener('submit', sendMessage, true);
messageInput.addEventListener('input', sendTypingStatus, true);
//document.getElementById('fileInput').addEventListener('change', handleFileUpload, true);
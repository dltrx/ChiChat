package dev.dltrx.chichat.config;

import dev.dltrx.chichat.service.Message;
import dev.dltrx.chichat.service.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChiChatEventListener {

    private final SimpMessageSendingOperations messageSendingOperations;

    @EventListener
    public void handleChiChatDisconnectListener(SessionDisconnectEvent disconnectEvent) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(disconnectEvent.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (Objects.nonNull(username)) {
            log.info("User disconnected: {}", username);

            messageSendingOperations.convertAndSend("/topic/chat", Message.builder().type(MessageType.LEAVE).sender(username).build());
        }
    }

}

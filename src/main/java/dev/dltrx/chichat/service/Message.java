package dev.dltrx.chichat.service;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    private MessageType type;

    private String content;

    private String sender;
}

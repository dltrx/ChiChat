package dev.dltrx.chichat.service;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TypingStatus {
    private String sender;
    private boolean typing;
}
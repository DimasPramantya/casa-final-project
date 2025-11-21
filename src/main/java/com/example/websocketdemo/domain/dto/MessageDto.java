package com.example.websocketdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private MessageType type;
    private String message;
    private String userTargetId;
    private String senderId;
    private String senderName;
}

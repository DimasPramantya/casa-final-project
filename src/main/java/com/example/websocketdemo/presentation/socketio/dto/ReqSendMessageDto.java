package com.example.websocketdemo.presentation.socketio.dto;

import lombok.Data;

@Data
public class ReqSendMessageDto {
    private MessageType type;
    private String message;
    private String userTargetId;

    public ReqSendMessageDto() {
    }

    public ReqSendMessageDto(MessageType type, String message, String userTargetId) {
        this.type = type;
        this.message = message;
        this.userTargetId = userTargetId;
    }
}
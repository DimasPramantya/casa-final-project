package com.example.websocketdemo.presentation.socketio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectionDto {
    private String machineId;
    private String queueName;
}

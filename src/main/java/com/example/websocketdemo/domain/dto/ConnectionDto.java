package com.example.websocketdemo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectionDto {
    private String machineId;
    private String queueName;
    private String username;
}

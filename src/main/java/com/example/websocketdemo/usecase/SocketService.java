package com.example.websocketdemo.usecase;

import com.corundumstudio.socketio.SocketIOClient;
import com.example.websocketdemo.presentation.socketio.dto.MessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SocketService {

    public void sendMessage(MessageDto message, String eventName, SocketIOClient senderClient) {
        for (
            SocketIOClient client : senderClient.getNamespace().getRoomOperations(message.getUserTargetId()).getClients()) {
            if (!client.getSessionId().equals(senderClient.getSessionId())) {
                client.sendEvent(
                    eventName,
                    message
                );
            }
        }
    }

}


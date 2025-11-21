package com.example.websocketdemo.usecase;

import com.corundumstudio.socketio.SocketIOClient;
import com.example.websocketdemo.domain.dto.MessageDto;
import com.example.websocketdemo.domain.entity.Conversation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SocketService {

    private final ConversationService conversationService;

    public SocketService(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    public void sendMessage(MessageDto message, String eventName, SocketIOClient senderClient) {
        for (
            SocketIOClient client : senderClient.getNamespace().getRoomOperations(message.getUserTargetId()).getClients()) {
            if (!client.getSessionId().equals(senderClient.getSessionId())) {
                Conversation conversation = conversationService.getConversation(
                    Integer.parseInt(message.getSenderId()),
                    Integer.parseInt(message.getUserTargetId())
                );
                log.info("conv");
                log.info(String.valueOf(conversation));
                if(conversation == null){
                    conversation = conversationService.saveConversation(
                        Integer.parseInt(message.getSenderId()),
                        Integer.parseInt(message.getUserTargetId())
                    );
                }
                conversationService.saveConversationMessage(conversation, message);
                client.sendEvent(
                    eventName,
                    message
                );
            }
        }
    }

}


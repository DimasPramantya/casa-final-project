package com.example.websocketdemo.usecase;

import com.example.websocketdemo.domain.ChatMessage;
import com.example.websocketdemo.domain.Conversation;
import com.example.websocketdemo.domain.User;
import com.example.websocketdemo.presentation.socketio.dto.MessageDto;
import com.example.websocketdemo.repository.ChatMessageRepository;
import com.example.websocketdemo.repository.ConversationRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
public class ConversationService {
    private final ConversationRepository conversationRepository;
    private final UserService userService;
    private final ChatMessageRepository chatMessageRepository;

    public ConversationService(ConversationRepository conversationRepository, UserService userService, ChatMessageRepository chatMessageRepository) {
        this.conversationRepository = conversationRepository;
        this.userService = userService;
        this.chatMessageRepository = chatMessageRepository;
    }

    @Cacheable(value = "getConversation", key = "{#userOneId, #userTwoId}")
    public Conversation getConversation(Integer userOneId, Integer userTwoId){
        User userOne = userService.getUserById(userOneId);
        User userTwo = userService.getUserById(userTwoId);
        return conversationRepository.findByUserOneAndUserTwo(userOne, userTwo);
    }

    public void saveConversationMessage(
        Conversation conversation,
        MessageDto message
    ) {
        chatMessageRepository.save(
            ChatMessage.builder()
                .createdAt(Timestamp.from(Instant.now()))
                .conversation(conversation)
                .content(message.getMessage())
                .senderId(Integer.parseInt(message.getSenderId()))
            .build()
        );
    }

    @Cacheable(value = "getConversation", key = "{#userOneId, #userTwoId}")
    public Conversation saveConversation(Integer userOneId, Integer userTwoId){
        User userOne = userService.getUserById(userOneId);
        User userTwo = userService.getUserById(userTwoId);
        Conversation conversation = Conversation.builder()
                .userOne(userOne)
                .userTwo(userTwo)
                .build();
        return conversationRepository.save(conversation);
    }
}

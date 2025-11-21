package com.example.websocketdemo.presentation.rest;

import com.example.websocketdemo.domain.entity.ChatMessage;
import com.example.websocketdemo.usecase.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Penting agar frontend bisa akses
public class ChatController {

    private final ConversationService conversationService;

    @GetMapping("/{userOneId}/{userTwoId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(
            @PathVariable Integer userOneId,
            @PathVariable Integer userTwoId
    ) {
        List<ChatMessage> history = conversationService.getChatHistory(userOneId, userTwoId);
        return ResponseEntity.ok(history);
    }
}

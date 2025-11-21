package com.example.websocketdemo.repository;

import com.example.websocketdemo.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    @Query(value = """
        SELECT * FROM chat_messages m
        ORDER BY m.created_at DESC
    """, nativeQuery = true)
    List<ChatMessage> findAllByOrderByCreatedAtDesc();
}
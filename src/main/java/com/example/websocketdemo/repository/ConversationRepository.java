package com.example.websocketdemo.repository;

import com.example.websocketdemo.domain.entity.Conversation;
import com.example.websocketdemo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    @Query("""
        SELECT C FROM Conversation C
        WHERE (C.userOne = :userOne or C.userOne = :userTwo) AND 
        (C.userTwo = :userTwo or C.userTwo = :userOne)
    """)
    Optional<Conversation> findByUserOneAndUserTwo(User userOne, User userTwo);
}

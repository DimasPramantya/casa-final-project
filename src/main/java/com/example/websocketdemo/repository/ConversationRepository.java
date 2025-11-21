package com.example.websocketdemo.repository;

import com.example.websocketdemo.domain.Conversation;
import com.example.websocketdemo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    @Query("""
        SELECT C FROM Conversation C
        WHERE (C.userOne = :userOne or C.userOne = :userTwo) AND 
        (C.userTwo = :userTwo or C.userTwo = :userOne)
    """)
    Conversation findByUserOneAndUserTwo(User userOne, User userTwo);
}

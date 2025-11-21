package com.example.websocketdemo.repository;

import com.example.websocketdemo.domain.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    @Query(value = """
        SELECT * FROM conversations C
        WHERE (C.user_id_one = :userOneId or C.user_id_one = :userTwoId) AND 
        (C.user_id_two = :userTwoId or C.user_id_two = :userOneId)
    """, nativeQuery = true)
    Optional<Conversation> findByUserOneAndUserTwo(Integer userOneId, Integer userTwoId);
}

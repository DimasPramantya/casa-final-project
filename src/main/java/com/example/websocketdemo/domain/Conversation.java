package com.example.websocketdemo.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_one", referencedColumnName = "id")
    private User userOne;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id_two", referencedColumnName = "id")
    private User userTwo;
}

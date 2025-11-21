package com.example.websocketdemo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "conversations")
public class Conversation implements Serializable {
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

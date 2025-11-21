package com.example.websocketdemo.repository;

import com.example.websocketdemo.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}

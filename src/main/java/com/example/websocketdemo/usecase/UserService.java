package com.example.websocketdemo.usecase;

import com.example.websocketdemo.domain.User;
import com.example.websocketdemo.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Cacheable(value = "userById", key = "{id}")
    public User getUserById(Integer id){
        return userRepository.findById(id).orElseThrow(
            () -> new RuntimeException("User not found")
        );
    }
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @CacheEvict(value = "userById", key = "{id}")
    public User saveUser(String userId, String username){
        Integer userIdInt = Integer.parseInt(userId);
        User user = userRepository.findById(userIdInt).orElse(null);
        if(user == null){
            user = new User(
                userIdInt,
                username
            );
            user = userRepository.save(user);
        }else{

            if(!user.getUsername().equals(username)){
                user.setUsername(username);
                user = userRepository.save(user);
            }
        }
        return user;
    }
}

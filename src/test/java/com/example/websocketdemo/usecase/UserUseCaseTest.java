package com.example.websocketdemo.usecase;


import com.example.websocketdemo.domain.entity.User;
import com.example.websocketdemo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.verify;
@ExtendWith(MockitoExtension.class)
public class UserUseCaseTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should successfully save new user")
    public void shouldSuccessfullySaveNewUser() {
        Integer userId = 1;
        String username = "test";

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(
                invocation-> invocation.getArgument(0, User.class)
        );

        User result = userService.saveUser(userId.toString(), username);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(username, result.getUsername());

        verify(userRepository).save(argThat(user -> {
            assertNotNull(user);
            return true;
        }));

    }


}

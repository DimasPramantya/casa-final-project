package com.example.websocketdemo.presentation.socketio.config;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ServerCommandLineRunner implements CommandLineRunner {
    private final SocketIOServer server;
    @Override
    public void run(String... args) throws Exception {
        try{
            server.start();
            log.info("Socket.IO server started on {}:{}", server.getConfiguration().getHostname(), server.getConfiguration().getPort());
        } catch (Exception e){
            log.error("Error starting Socket.IO server: {}", e.getMessage());
        }
    }
}
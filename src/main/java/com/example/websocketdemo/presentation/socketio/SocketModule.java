package com.example.websocketdemo.presentation.socketio;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.example.websocketdemo.domain.User;
import com.example.websocketdemo.infrastucture.redis.RedisConnectionService;
import com.example.websocketdemo.presentation.socketio.dto.ConnectionDto;
import com.example.websocketdemo.presentation.socketio.dto.MessageDto;
import com.example.websocketdemo.presentation.socketio.dto.ReqSendMessageDto;
import com.example.websocketdemo.usecase.SocketService;
import com.example.websocketdemo.usecase.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SocketModule {


    private final SocketIOServer server;
    private final SocketService socketService;
    private final RedisConnectionService redisConnectionService;
    private final String machineId = UUID.randomUUID().toString();

    private final Map<String, ConnectionDto> sessionToRedisKey = new ConcurrentHashMap<>();
    private final UserService userService;

    public SocketModule(SocketIOServer server, SocketService socketService, RedisConnectionService redisConnectionService, UserService userService) {
        this.server = server;
        this.socketService = socketService;
        this.redisConnectionService = redisConnectionService;
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("send_message", ReqSendMessageDto.class, onChatReceived());
        server.addEventListener("heartbeat", ReqSendMessageDto.class, onHeartbeatReceived());
        this.userService = userService;
    }

    private DataListener<ReqSendMessageDto> onHeartbeatReceived() {
        return (senderClient, data, ackSender) -> {
            String userId = senderClient.getAllRooms().stream().findFirst().orElse(null);
            if (userId != null) {
                log.debug("Heartbeat received from User[{}]", userId);
                User user = userService.getUserById(Integer.parseInt(userId));
                ConnectionDto connectionDto = sessionToRedisKey.get(userId);
                if (connectionDto == null) {
                    connectionDto = new ConnectionDto(machineId,  user.getUsername(),"queue-1");
                }
                redisConnectionService.saveConnection(userId, connectionDto);
            }
        };
    }


    private DataListener<ReqSendMessageDto> onChatReceived() {
        return (senderClient, data, ackSender) -> {
            log.info(data.toString());
            HandshakeData handshake = senderClient.getHandshakeData();
            String userId = handshake.getSingleUrlParam("userId");
            User sender = userService.getUserById(Integer.parseInt(userId));
            MessageDto messageDto = MessageDto
                .builder()
                .senderId(userId)
                .senderName(sender.getUsername())
                .userTargetId(data.getUserTargetId())
                .message(data.getMessage())
                .build();
            socketService.sendMessage(messageDto,"get_message", senderClient);
        };
    }


    private ConnectListener onConnected() {
        return (client) -> {
            HandshakeData handshake = client.getHandshakeData();
            String userId = handshake.getSingleUrlParam("userId");
            client.joinRoom(userId);
            String username = handshake.getSingleUrlParam("userName");
            if (userId == null) {
                userId = handshake.getSingleUrlParam("userId");
            }

            String redisKey = userId;

            ConnectionDto connectionDto = new ConnectionDto(
                    machineId, "queue-"+machineId, username
            );
            userService.saveUser(userId, username);

            if (redisKey != null && !redisKey.trim().isEmpty()) {
                client.set("userId", redisKey);
                redisConnectionService.saveConnection(redisKey, connectionDto);
                sessionToRedisKey.put(userId, connectionDto);
                log.info("Socket[{}] connected. Saved to Redis with key={} user_id={} machine_id={}", machineId, redisKey, userId, machineId);
            } else {
                log.warn("Socket[{}] connected without user_id param; skipping Redis save", machineId);
            }

            log.info("Socket ID[{}] Connected to socket", machineId);
        };

    }

    private DisconnectListener onDisconnected() {
        return client -> {
            HandshakeData handshake = client.getHandshakeData();

            String userId = handshake.getSingleUrlParam("user_id");
            if (userId == null) {
                userId = handshake.getSingleUrlParam("userId");
            }

            String machineId = client.getSessionId().toString();
            if (userId != null && !userId.trim().isEmpty()) {
                ConnectionDto redisKey = sessionToRedisKey.remove(userId);
                if (redisKey != null) {
                    redisConnectionService.deleteConnection(userId);
                }
            } else {
                log.debug("Client disconnected but userId was null (likely connection failed or anonymous)");
            }
            log.info("Client[{}] - Disconnected from socket", machineId);
        };
    }
}
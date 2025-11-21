package com.example.websocketdemo.presentation.socketio;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.example.websocketdemo.infrastucture.redis.RedisConnectionService;
import com.example.websocketdemo.presentation.socketio.dto.ConnectionDto;
import com.example.websocketdemo.presentation.socketio.dto.Message;
import com.example.websocketdemo.usecase.SocketService;
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

    public SocketModule(SocketIOServer server, SocketService socketService, RedisConnectionService redisConnectionService) {
        this.server = server;
        this.socketService = socketService;
        this.redisConnectionService = redisConnectionService;
        server.addConnectListener(onConnected());
        server.addDisconnectListener(onDisconnected());
        server.addEventListener("send_message", Message.class, onChatReceived());
        server.addEventListener("heartbeat", Message.class, onHeartbeatReceived());
    }

    private DataListener<Message> onHeartbeatReceived() {
        return (senderClient, data, ackSender) -> {
            String userId = senderClient.get("userId");
            if (userId != null) {
                log.debug("Heartbeat received from User[{}]", userId);
                ConnectionDto connectionDto = sessionToRedisKey.get(userId);
                if (connectionDto == null) {
                    connectionDto = new ConnectionDto(machineId, "queue-1");
                }
                redisConnectionService.saveConnection(userId, connectionDto);
            }
        };
    }


    private DataListener<Message> onChatReceived() {
        return (senderClient, data, ackSender) -> {
            log.info(data.toString());
            socketService.sendMessage(data.getRoom(),"get_message", senderClient, data.getMessage());
        };
    }


    private ConnectListener onConnected() {
        return (client) -> {
            HandshakeData handshake = client.getHandshakeData();
            String room = handshake.getSingleUrlParam("room");
            if (room != null) {
                client.joinRoom(room);
            }

            String userId = coalesce(
                    headerOrNull(handshake, "userId"),
                    handshake.getSingleUrlParam("userId")
            );

            String redisKey = firstNonBlank(userId);
            ConnectionDto connectionDto = new ConnectionDto(
                    machineId, "queue-"+machineId
            );

            if (redisKey != null) {
                redisConnectionService.saveConnection(redisKey,connectionDto);
                sessionToRedisKey.put(userId, connectionDto); // map socket session -> redis key
                log.info("Socket[{}] connected. Saved to Redis with key={} user_id={} machine_id={}", machineId, redisKey, userId, machineId);
            } else {
                log.warn("Socket[{}] connected without session_id or user_id; skipping Redis save", machineId);
            }

            log.info("Socket ID[{}] Connected to socket", machineId);
        };

    }

    private DisconnectListener onDisconnected() {
        return client -> {
            HandshakeData handshake = client.getHandshakeData();
            String userId = coalesce(
                    headerOrNull(handshake, "userId"),
                    handshake.getSingleUrlParam("userId")
            );
            String machineId = client.getSessionId().toString();
            ConnectionDto redisKey = sessionToRedisKey.remove(userId);
            if (redisKey != null) {
                redisConnectionService.deleteConnection(userId);
            }
            log.info("Client[{}] - Disconnected from socket", machineId);
        };
    }

    private String headerOrNull(HandshakeData handshake, String key) {
        try {
            String val = handshake.getHttpHeaders().get(key);
            return isBlank(val) ? null : val;
        } catch (Exception e) {
            return null;
        }
    }

    private String firstNonBlank(String... vals) {
        if (vals == null) return null;
        for (String v : vals) {
            if (!isBlank(v)) return v;
        }
        return null;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String coalesce(String a, String b) {
        return !isBlank(a) ? a : b;
    }
}

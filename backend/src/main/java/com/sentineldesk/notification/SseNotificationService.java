package com.sentineldesk.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseNotificationService {

    private static final Logger log = LoggerFactory.getLogger(SseNotificationService.class);
    private static final Long DEFAULT_TIMEOUT = 300_000L; // 5 minutes

    // Map of userId -> list of active emitters (user might have multiple browser tabs open)
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();
    // Global emitters for system-wide broadcasts (e.g. ticket queue, SLA breaches)
    private final CopyOnWriteArrayList<SseEmitter> broadcastEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        if (userId != null) {
            userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);
        }
        broadcastEmitters.add(emitter);

        Runnable cleanup = () -> {
            if (userId != null && userEmitters.containsKey(userId)) {
                userEmitters.get(userId).remove(emitter);
                if (userEmitters.get(userId).isEmpty()) {
                    userEmitters.remove(userId);
                }
            }
            broadcastEmitters.remove(emitter);
        };

        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        // Send immediate heartbeat on connection
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data(Map.of("message", "Live SentinelDesk stream active", "timestamp", System.currentTimeMillis())));
        } catch (IOException e) {
            cleanup.run();
        }

        return emitter;
    }

    public void sendToUser(Long userId, String eventName, Object data) {
        if (userId == null || !userEmitters.containsKey(userId)) return;

        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }

    public void broadcast(String eventName, Object data) {
        for (SseEmitter emitter : broadcastEmitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(data));
            } catch (Exception e) {
                broadcastEmitters.remove(emitter);
            }
        }
    }
}

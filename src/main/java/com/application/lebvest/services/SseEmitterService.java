package com.application.lebvest.services;

import com.application.lebvest.models.dtos.AdminNotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseEmitterService {

    private static final long SSE_TIMEOUT = 5 * 60 * 1000L; // 5 minutes

    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.add(emitter);

        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.debug("SSE emitter completed, active={}", emitters.size());
        });
        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.debug("SSE emitter timed out, active={}", emitters.size());
        });
        emitter.onError(ex -> {
            emitters.remove(emitter);
            log.debug("SSE emitter error: {}, active={}", ex.getMessage(), emitters.size());
        });

        log.debug("SSE emitter registered, active={}", emitters.size());
        return emitter;
    }

    public void broadcast(AdminNotificationDto notification) {
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .id(String.valueOf(notification.id()))
                .name("notification")
                .data(notification);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(event);
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }
}

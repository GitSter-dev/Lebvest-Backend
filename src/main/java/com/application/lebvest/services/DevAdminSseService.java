package com.application.lebvest.services;

import com.application.lebvest.services.interfaces.AdminSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Profile("dev")
@Service
@RequiredArgsConstructor
public class  DevAdminSseService implements AdminSseService {

    private static final long SSE_TIMEOUT_MS = 0L;
    private static final int MAX_STORED_EVENTS = 100;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final ConcurrentLinkedDeque<SseEvent> events = new ConcurrentLinkedDeque<>();
    private final AtomicLong eventIds = new AtomicLong();

    public record SseEvent(Long id, Object data) {
    }

    @Override
    public SseEmitter subscribeToAdminSseEvents(String lastEventId) {
        long lastSeenEventId = parseLastEventId(lastEventId);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onError(error -> {
            emitter.completeWithError(error);
            emitters.remove(emitter);
        });
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitters.add(emitter);

        events.stream()
                .filter(event -> event.id > lastSeenEventId)
                .forEach(event -> sendEvent(emitter, event));

        sendConnectedEvent(emitter);
        return emitter;
    }

    public void broadcast(Object data) {
        broadcast(eventIds.incrementAndGet(), data);
    }

    public void broadcast(Long id, Object data) {
        SseEvent event = new SseEvent(id, data);
        rememberEvent(event);
        emitters.forEach(emitter -> sendEvent(emitter, event));
    }

    private long parseLastEventId(String lastEventId) {
        if (lastEventId == null || lastEventId.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(lastEventId.trim());
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private void rememberEvent(SseEvent event) {
        events.addLast(event);
        while (events.size() > MAX_STORED_EVENTS) {
            events.pollFirst();
        }
    }

    private void sendConnectedEvent(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data("subscribed"));
        } catch (IOException error) {
            emitter.completeWithError(error);
            emitters.remove(emitter);
        }
    }

    private void sendEvent(SseEmitter emitter, SseEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .id(String.valueOf(event.id))
                    .name("admin-event")
                    .data(event.data));
        } catch (IOException error) {
            emitter.completeWithError(error);
            emitters.remove(emitter);
        }
    }
}

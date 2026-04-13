package com.application.lebvest.controllers;

import com.application.lebvest.models.dtos.*;
import com.application.lebvest.services.AdminNotificationService;
import com.application.lebvest.services.SseEmitterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;
    private final SseEmitterService sseEmitterService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<CursorPageResponseDto<AdminNotificationDto>>> getNotifications(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        CursorPageResponseDto<AdminNotificationDto> page = adminNotificationService.getNotifications(cursor, size);
        return ResponseEntity.ok(ApiResponseDto.ok(HttpStatus.OK.value(), page));
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(
            @RequestHeader(name = "Last-Event-ID", required = false) String lastEventIdHeader,
            @RequestParam(name = "lastEventId", required = false) Long lastEventIdParam
    ) {
        SseEmitter emitter = sseEmitterService.createEmitter();

        Long lastEventId = null;
        if (lastEventIdHeader != null && !lastEventIdHeader.isBlank()) {
            try {
                lastEventId = Long.parseLong(lastEventIdHeader.trim());
            } catch (NumberFormatException ignored) {}
        }
        if (lastEventId == null && lastEventIdParam != null) {
            lastEventId = lastEventIdParam;
        }

        if (lastEventId != null) {
            List<AdminNotificationDto> missed = adminNotificationService.getMissedNotifications(lastEventId);
            for (AdminNotificationDto n : missed) {
                try {
                    emitter.send(SseEmitter.event()
                            .id(String.valueOf(n.id()))
                            .name("notification")
                            .data(n));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                    return emitter;
                }
            }
        }

        return emitter;
    }

    @PatchMapping("/read")
    public ResponseEntity<ApiResponseDto<Integer>> markAsRead(
            @Valid @RequestBody MarkAsReadRequestDto request
    ) {
        int updated = adminNotificationService.markAsRead(request.ids());
        return ResponseEntity.ok(ApiResponseDto.ok(HttpStatus.OK.value(), updated));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponseDto<Integer>> markAllAsRead() {
        int updated = adminNotificationService.markAllAsRead();
        return ResponseEntity.ok(ApiResponseDto.ok(HttpStatus.OK.value(), updated));
    }
}

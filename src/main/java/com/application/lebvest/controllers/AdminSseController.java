package com.application.lebvest.controllers;

import com.application.lebvest.services.interfaces.AdminSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Profile("dev")
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class AdminSseController {

    private final AdminSseService adminSseService;

    @GetMapping(path = "/", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToSseEvents(
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId
    ) {
        return adminSseService.subscribeToAdminSseEvents(lastEventId);
    }
}

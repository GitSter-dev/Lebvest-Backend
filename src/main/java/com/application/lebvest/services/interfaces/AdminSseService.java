package com.application.lebvest.services.interfaces;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AdminSseService {

    SseEmitter subscribeToAdminSseEvents(String lastEventId);
}

package com.application.lebvest.models.exceptions;

import lombok.Getter;

@Getter
public class MailMessagingException extends RuntimeException {
    private final String recipient;

    public MailMessagingException(String message, String recipient, Throwable cause) {
        super(message);
        this.recipient = recipient;
        initCause(cause);
    }
}

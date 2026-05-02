package com.application.auth_lebvest.models.exceptions;

import lombok.Getter;

public class Exceptions {

    @Getter
    public static class ResourceConflictException extends RuntimeException {
        private final Class<?> clazz;
        private final String field;
        private final Object value;

        public ResourceConflictException(Class<?> clazz, String field, Object value) {
            super(String.format("%s already exists with %s: %s", clazz.getSimpleName(), field, value));
            this.clazz = clazz;
            this.field = field;
            this.value = value;
        }

    }
}

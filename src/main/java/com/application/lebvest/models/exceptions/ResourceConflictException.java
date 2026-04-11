package com.application.lebvest.models.exceptions;

public class ResourceConflictException extends RuntimeException {

    private final Class<?> resourceClass;
    private final String field;
    private final Object value;

    public ResourceConflictException(Class<?> resourceClass, String field, Object value) {
        super(buildMessage(resourceClass, field, value));
        this.resourceClass = resourceClass;
        this.field = field;
        this.value = value;
    }

    private static String buildMessage(Class<?> resourceClass, String field, Object value) {
        return resourceClass.getSimpleName()
                + " already exists with "
                + field
                + " = "
                + value;
    }
}
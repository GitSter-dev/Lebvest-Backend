package com.application.lebvest.models.exceptions;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Class<?> resourceClass, String field, Object value) {
        super(buildMessage(resourceClass, field, value));
    }

    private static String buildMessage(Class<?> resourceClass, String field, Object value) {
        return resourceClass.getSimpleName()
                + " not found with "
                + field
                + " = "
                + value;
    }
}

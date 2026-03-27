package com.application.lebvest.models.exceptions;


public class ResourceConflictException extends RuntimeException {

    public ResourceConflictException(Class<?> resourceClass, String conflictField, Object value) {
        super(resourceClass.getSimpleName() + " with " + conflictField + " '" + value + "' already exists");
    }
}

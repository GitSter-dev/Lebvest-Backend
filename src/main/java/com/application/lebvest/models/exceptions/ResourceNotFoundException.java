package com.application.lebvest.models.exceptions;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    final Class<?> resourceClass;
    final String resourceField;
    final Object value;

    public ResourceNotFoundException(Class<?> resourceClass, String resourceField, Object value) {
        super("Value " + value + " not found on field " + resourceField + " for entity " + resourceClass.getSimpleName());
        this.resourceClass = resourceClass;
        this.resourceField = resourceField;
        this.value = value;
    }
}

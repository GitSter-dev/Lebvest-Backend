package com.application.lebvest.utils;

import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class PathGenerator {

    public static String generateUserDocumentPath(String prefix, String userId, String extensionName) {
        return prefix + "/" + (userId == null ? UUID.randomUUID() : userId) + "/" + UUID.randomUUID() + (extensionName == null ? "" : "." + extensionName);
    }
}

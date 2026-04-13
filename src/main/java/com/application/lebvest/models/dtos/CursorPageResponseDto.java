package com.application.lebvest.models.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record CursorPageResponseDto<T>(
        List<T> items,
        Long nextCursor,
        boolean hasMore,
        long totalUnread
) {
}

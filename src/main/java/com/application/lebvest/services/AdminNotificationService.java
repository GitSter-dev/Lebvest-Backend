package com.application.lebvest.services;

import com.application.lebvest.models.dtos.AdminNotificationDto;
import com.application.lebvest.models.dtos.CursorPageResponseDto;
import com.application.lebvest.models.entities.AdminNotification;
import com.application.lebvest.models.enums.AdminNotificationType;
import com.application.lebvest.repositories.AdminNotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminNotificationService {

    private final AdminNotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    public AdminNotificationDto createAndBroadcast(
            String title, String content,
            AdminNotificationType type, Map<String, Object> data
    ) {
        AdminNotification entity = AdminNotification.builder()
                .title(title)
                .content(content)
                .type(type)
                .data(data)
                .build();

        entity = notificationRepository.save(entity);
        AdminNotificationDto dto = toDto(entity);
        sseEmitterService.broadcast(dto);
        log.info("Notification created and broadcast id={} type={}", entity.getId(), type);
        return dto;
    }

    public CursorPageResponseDto<AdminNotificationDto> getNotifications(Long cursor, int size) {
        List<AdminNotification> results;
        if (cursor != null) {
            results = notificationRepository.findByIdLessThanOrderByIdDesc(cursor, PageRequest.of(0, size + 1));
        } else {
            results = notificationRepository.findAllByOrderByIdDesc(PageRequest.of(0, size + 1));
        }

        boolean hasMore = results.size() > size;
        if (hasMore) {
            results = results.subList(0, size);
        }

        Long nextCursor = hasMore && !results.isEmpty()
                ? results.getLast().getId()
                : null;

        long totalUnread = notificationRepository.countByReadFalse();

        return CursorPageResponseDto.<AdminNotificationDto>builder()
                .items(results.stream().map(this::toDto).toList())
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .totalUnread(totalUnread)
                .build();
    }

    @Transactional
    public int markAsRead(List<Long> ids) {
        return notificationRepository.markAsReadByIds(ids);
    }

    @Transactional
    public int markAllAsRead() {
        return notificationRepository.markAllAsRead();
    }

    public List<AdminNotificationDto> getMissedNotifications(Long lastEventId) {
        return notificationRepository.findByIdGreaterThanOrderByIdAsc(lastEventId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private AdminNotificationDto toDto(AdminNotification entity) {
        return AdminNotificationDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .type(entity.getType())
                .read(entity.isRead())
                .data(entity.getData())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

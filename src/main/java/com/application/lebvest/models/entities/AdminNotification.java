package com.application.lebvest.models.entities;

import com.application.lebvest.models.enums.AdminNotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "admin_notifications")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "admin_notification_type")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private AdminNotificationType type;

    @Column(nullable = false)
    @Builder.Default
    private boolean read = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;
}

package com.application.lebvest.repositories;

import com.application.lebvest.models.entities.AdminNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    List<AdminNotification> findByIdLessThanOrderByIdDesc(Long cursor, Pageable pageable);

    List<AdminNotification> findAllByOrderByIdDesc(Pageable pageable);

    long countByReadFalse();

    List<AdminNotification> findByIdGreaterThanOrderByIdAsc(Long lastEventId);

    @Modifying
    @Query("UPDATE AdminNotification n SET n.read = true WHERE n.id IN :ids AND n.read = false")
    int markAsReadByIds(List<Long> ids);

    @Modifying
    @Query("UPDATE AdminNotification n SET n.read = true WHERE n.read = false")
    int markAllAsRead();
}

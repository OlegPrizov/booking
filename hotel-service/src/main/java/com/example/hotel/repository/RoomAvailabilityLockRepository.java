package com.example.hotel.repository;

import com.example.hotel.entity.RoomAvailabilityLock;
import com.example.hotel.entity.RoomAvailabilityLock.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public interface RoomAvailabilityLockRepository extends JpaRepository<RoomAvailabilityLock, Long> {

    Optional<RoomAvailabilityLock> findByRequestId(String requestId);

    @Query("""
           select count(l) > 0
           from RoomAvailabilityLock l
           where l.roomId = :roomId
             and l.status in :activeStatuses
             and l.startDate < :endDate
             and :startDate < l.endDate
           """)
    boolean existsOverlap(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("activeStatuses") Collection<Status> activeStatuses
    );
}
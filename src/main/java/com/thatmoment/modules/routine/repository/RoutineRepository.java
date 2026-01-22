package com.thatmoment.modules.routine.repository;

import com.thatmoment.modules.routine.domain.Routine;
import com.thatmoment.modules.routine.domain.enums.RoutineDayOfWeek;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineRepository extends JpaRepository<Routine, UUID> {

    Optional<Routine> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    boolean existsByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    @Query("""
        select r from Routine r
        where r.userId = :userId
          and r.deletedAt is null
          and (:isActive is null or r.isActive = :isActive)
          and (
              :query is null
              or lower(r.title) like lower(concat('%', :query, '%'))
              or lower(r.description) like lower(concat('%', :query, '%'))
          )
        """)
    Page<Routine> search(
            @Param("userId") UUID userId,
            @Param("isActive") Boolean isActive,
            @Param("query") String query,
            Pageable pageable
    );

    List<Routine> findByUserIdAndIsActiveTrueAndDeletedAtIsNull(UUID userId);

    long countByUserIdAndIsActiveTrueAndDeletedAtIsNull(UUID userId);

    @Query("""
        select r from Routine r
        where r.userId = :userId
          and r.deletedAt is null
          and r.isActive = true
          and (r.startDate is null or r.startDate <= :date)
          and (r.endDate is null or r.endDate >= :date)
          and exists (
              select 1 from RoutineSchedule s
              where s.routineId = r.id and s.dayOfWeek = :dayOfWeek
          )
        """)
    List<Routine> findTodayRoutines(
            @Param("userId") UUID userId,
            @Param("date") LocalDate date,
            @Param("dayOfWeek") RoutineDayOfWeek dayOfWeek
    );
}

package com.thatmoment.modules.plan.repository;

import com.thatmoment.modules.plan.domain.PlanCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanCategoryRepository extends JpaRepository<PlanCategory, UUID> {

    Optional<PlanCategory> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    Page<PlanCategory> findByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    boolean existsByUserIdAndNameAndDeletedAtIsNull(UUID userId, String name);

    boolean existsByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);
}

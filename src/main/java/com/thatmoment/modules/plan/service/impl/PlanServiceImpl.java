package com.thatmoment.modules.plan.service.impl;

import com.thatmoment.common.dto.MessageResponse;
import com.thatmoment.common.exception.exceptions.NotFoundException;
import com.thatmoment.modules.plan.constants.PlanMessages;
import com.thatmoment.modules.plan.domain.Plan;
import com.thatmoment.modules.plan.dto.request.CreatePlanRequest;
import com.thatmoment.modules.plan.dto.request.UpdatePlanRequest;
import com.thatmoment.modules.plan.dto.response.PlanResponse;
import com.thatmoment.modules.plan.mapper.PlanMapper;
import com.thatmoment.modules.plan.repository.PlanCategoryRepository;
import com.thatmoment.modules.plan.repository.PlanRepository;
import com.thatmoment.modules.plan.service.PlanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
class PlanServiceImpl implements PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;
    private final PlanCategoryRepository categoryRepository;

    PlanServiceImpl(
            PlanRepository planRepository,
            PlanMapper planMapper,
            PlanCategoryRepository categoryRepository
    ) {
        this.planRepository = planRepository;
        this.planMapper = planMapper;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public PlanResponse createPlan(UUID userId, CreatePlanRequest request) {
        requireCategory(userId, request.categoryId());
        Plan plan = Plan.builder()
                .userId(userId)
                .categoryId(request.categoryId())
                .title(request.title())
                .description(request.description())
                .planDate(request.planDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();

        return planMapper.toResponse(planRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlan(UUID userId, UUID planId) {
        return planMapper.toResponse(getPlanEntity(userId, planId));
    }

    @Transactional(readOnly = true)
    public Page<PlanResponse> listPlans(UUID userId, LocalDate date, Boolean completed, Pageable pageable) {
        Page<Plan> plans;
        if (date != null) {
            if (completed != null) {
                plans = planRepository.findByUserIdAndPlanDateAndIsCompletedAndDeletedAtIsNull(
                        userId,
                        date,
                        completed,
                        pageable
                );
            } else {
                plans = planRepository.findByUserIdAndPlanDateAndDeletedAtIsNull(userId, date, pageable);
            }
        } else {
            if (completed != null) {
                plans = planRepository.findByUserIdAndIsCompletedAndDeletedAtIsNull(userId, completed, pageable);
            } else {
                plans = planRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
            }
        }
        return plans.map(planMapper::toResponse);
    }

    @Transactional
    public PlanResponse updatePlan(UUID userId, UUID planId, UpdatePlanRequest request) {
        requireCategory(userId, request.categoryId());
        Plan plan = getPlanEntity(userId, planId);
        plan.updateDetails(
                request.title(),
                request.description(),
                request.planDate(),
                request.startTime(),
                request.endTime(),
                request.categoryId()
        );
        return planMapper.toResponse(plan);
    }

    @Transactional
    public void deletePlan(UUID userId, UUID planId) {
        Plan plan = getPlanEntity(userId, planId);
        plan.softDelete(userId, null);
    }

    @Transactional
    public MessageResponse completePlan(UUID userId, UUID planId) {
        Plan plan = getPlanEntity(userId, planId);
        plan.complete();
        return MessageResponse.of(PlanMessages.PLAN_COMPLETED);
    }

    @Transactional
    public MessageResponse uncompletePlan(UUID userId, UUID planId) {
        Plan plan = getPlanEntity(userId, planId);
        plan.uncomplete();
        return MessageResponse.of(PlanMessages.PLAN_UNCOMPLETED);
    }

    @Transactional(readOnly = true)
    public long countPlans(UUID userId, LocalDate from, LocalDate to) {
        return planRepository.countByUserIdAndPlanDateBetweenAndDeletedAtIsNull(userId, from, to);
    }

    @Transactional(readOnly = true)
    public long countCompletedPlans(UUID userId, LocalDate from, LocalDate to) {
        return planRepository.countByUserIdAndPlanDateBetweenAndIsCompletedTrueAndDeletedAtIsNull(userId, from, to);
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, Long> countPlansByDate(UUID userId, LocalDate from, LocalDate to) {
        return toDateCountMap(planRepository.countTotalsByDate(userId, from, to));
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, Long> countCompletedPlansByDate(UUID userId, LocalDate from, LocalDate to) {
        return toDateCountMap(planRepository.countCompletedByDate(userId, from, to));
    }

    private Plan getPlanEntity(UUID userId, UUID planId) {
        return planRepository.findByIdAndUserIdAndDeletedAtIsNull(planId, userId)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
    }

    private Map<LocalDate, Long> toDateCountMap(List<Object[]> results) {
        Map<LocalDate, Long> counts = new HashMap<>();
        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            Number count = (Number) row[1];
            counts.put(date, count.longValue());
        }
        return counts;
    }

    private void requireCategory(UUID userId, UUID categoryId) {
        boolean exists = categoryRepository.existsByIdAndUserIdAndDeletedAtIsNull(categoryId, userId);
        if (!exists) {
            throw new NotFoundException("Category not found");
        }
    }
}

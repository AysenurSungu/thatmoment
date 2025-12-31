package com.thatmoment.modules.plan.service;

import com.thatmoment.modules.common.exception.exceptions.NotFoundException;
import com.thatmoment.modules.plan.domain.Plan;
import com.thatmoment.modules.plan.dto.request.CreatePlanRequest;
import com.thatmoment.modules.plan.dto.request.UpdatePlanRequest;
import com.thatmoment.modules.plan.repository.PlanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional
    public Plan createPlan(UUID userId, CreatePlanRequest request) {
        Plan plan = Plan.builder()
                .userId(userId)
                .title(request.title())
                .description(request.description())
                .planDate(request.planDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .color(request.color())
                .build();

        return planRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public Plan getPlan(UUID userId, UUID planId) {
        return planRepository.findByIdAndUserIdAndDeletedAtIsNull(planId, userId)
                .orElseThrow(() -> new NotFoundException("Plan not found"));
    }

    @Transactional(readOnly = true)
    public Page<Plan> listPlans(UUID userId, LocalDate date, Pageable pageable) {
        if (date != null) {
            return planRepository.findByUserIdAndPlanDateAndDeletedAtIsNull(userId, date, pageable);
        }
        return planRepository.findByUserIdAndDeletedAtIsNull(userId, pageable);
    }

    @Transactional
    public Plan updatePlan(UUID userId, UUID planId, UpdatePlanRequest request) {
        Plan plan = getPlan(userId, planId);
        plan.updateDetails(
                request.title(),
                request.description(),
                request.planDate(),
                request.startTime(),
                request.endTime(),
                request.color()
        );
        return planRepository.save(plan);
    }

    @Transactional
    public void deletePlan(UUID userId, UUID planId) {
        Plan plan = getPlan(userId, planId);
        plan.softDelete(userId, null);
        planRepository.save(plan);
    }
}

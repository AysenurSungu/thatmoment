package com.thatmoment.modules.plan.service;

import com.thatmoment.modules.plan.dto.request.CreatePlanRequest;
import com.thatmoment.modules.plan.dto.request.UpdatePlanRequest;
import com.thatmoment.common.dto.MessageResponse;
import com.thatmoment.modules.plan.dto.response.PlanResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

public interface PlanService {

    PlanResponse createPlan(UUID userId, CreatePlanRequest request);

    PlanResponse getPlan(UUID userId, UUID planId);

    Page<PlanResponse> listPlans(UUID userId, LocalDate date, Boolean completed, Pageable pageable);

    PlanResponse updatePlan(UUID userId, UUID planId, UpdatePlanRequest request);

    void deletePlan(UUID userId, UUID planId);

    MessageResponse completePlan(UUID userId, UUID planId);

    MessageResponse uncompletePlan(UUID userId, UUID planId);

    long countPlans(UUID userId, LocalDate from, LocalDate to);

    long countCompletedPlans(UUID userId, LocalDate from, LocalDate to);

    Map<LocalDate, Long> countPlansByDate(UUID userId, LocalDate from, LocalDate to);

    Map<LocalDate, Long> countCompletedPlansByDate(UUID userId, LocalDate from, LocalDate to);
}

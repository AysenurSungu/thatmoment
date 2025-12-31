package com.thatmoment.modules.plan.api;

import com.thatmoment.modules.auth.security.UserPrincipal;
import com.thatmoment.modules.common.constants.ApiDescriptions;
import com.thatmoment.modules.plan.dto.request.CreatePlanRequest;
import com.thatmoment.modules.plan.dto.request.UpdatePlanRequest;
import com.thatmoment.modules.plan.dto.response.PlanResponse;
import com.thatmoment.modules.plan.mapper.PlanMapper;
import com.thatmoment.modules.plan.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = ApiDescriptions.TAG_PLAN, description = ApiDescriptions.TAG_PLAN_DESC)
public class PlanController {

    private final PlanService planService;
    private final PlanMapper planMapper;

    public PlanController(PlanService planService, PlanMapper planMapper) {
        this.planService = planService;
        this.planMapper = planMapper;
    }

    @PostMapping
    @Operation(summary = ApiDescriptions.PLAN_CREATE_SUMMARY)
    public ResponseEntity<PlanResponse> createPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePlanRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PlanResponse response = planMapper.toResponse(
                planService.createPlan(principal.getUserId(), request)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = ApiDescriptions.PLAN_GET_SUMMARY)
    public ResponseEntity<PlanResponse> getPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PlanResponse response = planMapper.toResponse(
                planService.getPlan(principal.getUserId(), id)
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = ApiDescriptions.PLAN_LIST_SUMMARY)
    public ResponseEntity<Page<PlanResponse>> listPlans(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) LocalDate date
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by("planDate").ascending().and(Sort.by("startTime").ascending())
        );

        Page<PlanResponse> response = planService.listPlans(principal.getUserId(), date, pageRequest)
                .map(planMapper::toResponse);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = ApiDescriptions.PLAN_UPDATE_SUMMARY)
    public ResponseEntity<PlanResponse> updatePlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlanRequest request
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PlanResponse response = planMapper.toResponse(
                planService.updatePlan(principal.getUserId(), id, request)
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = ApiDescriptions.PLAN_DELETE_SUMMARY)
    public ResponseEntity<Void> deletePlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        planService.deletePlan(principal.getUserId(), id);
        return ResponseEntity.noContent().build();
    }
}

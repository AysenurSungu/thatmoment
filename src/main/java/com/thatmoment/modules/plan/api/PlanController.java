package com.thatmoment.modules.plan.api;

import com.thatmoment.modules.auth.security.UserPrincipal;
import com.thatmoment.common.constants.ApiDescriptions;
import com.thatmoment.modules.plan.dto.request.CreatePlanRequest;
import com.thatmoment.modules.plan.dto.request.UpdatePlanRequest;
import com.thatmoment.modules.plan.dto.response.PlanResponse;
import com.thatmoment.modules.plan.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
@Tag(name = ApiDescriptions.TAG_PLAN, description = ApiDescriptions.TAG_PLAN_DESC)
@PreAuthorize("isAuthenticated()")
public class PlanController {

    private final PlanService planService;

    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    @Operation(summary = ApiDescriptions.PLAN_CREATE_SUMMARY)
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse createPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePlanRequest request
    ) {
        return planService.createPlan(principal.getUserId(), request);
    }

    @GetMapping("/{id}")
    @Operation(summary = ApiDescriptions.PLAN_GET_SUMMARY)
    public PlanResponse getPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return planService.getPlan(principal.getUserId(), id);
    }

    @GetMapping
    @Operation(summary = ApiDescriptions.PLAN_LIST_SUMMARY)
    public Page<PlanResponse> listPlans(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "planDate", direction = Sort.Direction.ASC),
                    @SortDefault(sort = "startTime", direction = Sort.Direction.ASC)
            }) Pageable pageable,
            @RequestParam(required = false) LocalDate date
    ) {
        return planService.listPlans(principal.getUserId(), date, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = ApiDescriptions.PLAN_UPDATE_SUMMARY)
    public PlanResponse updatePlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlanRequest request
    ) {
        return planService.updatePlan(principal.getUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = ApiDescriptions.PLAN_DELETE_SUMMARY)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        planService.deletePlan(principal.getUserId(), id);
    }
}

package com.thatmoment.modules.plan.api;

import com.thatmoment.modules.auth.security.UserPrincipal;
import com.thatmoment.common.constants.ApiDescriptions;
import com.thatmoment.modules.plan.dto.request.CreatePlanCategoryRequest;
import com.thatmoment.modules.plan.dto.request.UpdatePlanCategoryRequest;
import com.thatmoment.modules.plan.dto.response.PlanCategoryResponse;
import com.thatmoment.modules.plan.service.PlanCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans/categories")
@Tag(name = ApiDescriptions.TAG_PLAN, description = ApiDescriptions.TAG_PLAN_DESC)
@PreAuthorize("isAuthenticated()")
public class PlanCategoryController {

    private final PlanCategoryService categoryService;

    public PlanCategoryController(PlanCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @Operation(summary = ApiDescriptions.PLAN_CATEGORY_CREATE_SUMMARY)
    @ResponseStatus(HttpStatus.CREATED)
    public PlanCategoryResponse createCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePlanCategoryRequest request
    ) {
        return categoryService.createCategory(principal.getUserId(), request);
    }

    @GetMapping("/{id}")
    @Operation(summary = ApiDescriptions.PLAN_CATEGORY_GET_SUMMARY)
    public PlanCategoryResponse getCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return categoryService.getCategory(principal.getUserId(), id);
    }

    @GetMapping
    @Operation(summary = ApiDescriptions.PLAN_CATEGORY_LIST_SUMMARY)
    public Page<PlanCategoryResponse> listCategories(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20)
            @SortDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return categoryService.listCategories(principal.getUserId(), pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = ApiDescriptions.PLAN_CATEGORY_UPDATE_SUMMARY)
    public PlanCategoryResponse updateCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlanCategoryRequest request
    ) {
        return categoryService.updateCategory(principal.getUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = ApiDescriptions.PLAN_CATEGORY_DELETE_SUMMARY)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        categoryService.deleteCategory(principal.getUserId(), id);
    }
}

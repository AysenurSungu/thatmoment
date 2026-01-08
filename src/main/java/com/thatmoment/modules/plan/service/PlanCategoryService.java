package com.thatmoment.modules.plan.service;

import com.thatmoment.modules.plan.dto.request.CreatePlanCategoryRequest;
import com.thatmoment.modules.plan.dto.request.UpdatePlanCategoryRequest;
import com.thatmoment.modules.plan.dto.response.PlanCategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PlanCategoryService {

    PlanCategoryResponse createCategory(UUID userId, CreatePlanCategoryRequest request);

    PlanCategoryResponse getCategory(UUID userId, UUID categoryId);

    Page<PlanCategoryResponse> listCategories(UUID userId, Pageable pageable);

    PlanCategoryResponse updateCategory(UUID userId, UUID categoryId, UpdatePlanCategoryRequest request);

    void deleteCategory(UUID userId, UUID categoryId);
}

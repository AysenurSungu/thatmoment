package com.thatmoment.modules.plan.service.impl;

import com.thatmoment.common.exception.exceptions.ConflictException;
import com.thatmoment.common.exception.exceptions.NotFoundException;
import com.thatmoment.modules.plan.domain.PlanCategory;
import com.thatmoment.modules.plan.dto.request.CreatePlanCategoryRequest;
import com.thatmoment.modules.plan.dto.request.UpdatePlanCategoryRequest;
import com.thatmoment.modules.plan.dto.response.PlanCategoryResponse;
import com.thatmoment.modules.plan.mapper.PlanCategoryMapper;
import com.thatmoment.modules.plan.repository.PlanCategoryRepository;
import com.thatmoment.modules.plan.service.PlanCategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
class PlanCategoryServiceImpl implements PlanCategoryService {

    private final PlanCategoryRepository categoryRepository;
    private final PlanCategoryMapper categoryMapper;

    PlanCategoryServiceImpl(
            PlanCategoryRepository categoryRepository,
            PlanCategoryMapper categoryMapper
    ) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public PlanCategoryResponse createCategory(UUID userId, CreatePlanCategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByUserIdAndNameAndDeletedAtIsNull(userId, name)) {
            throw new ConflictException("Category name already exists");
        }

        PlanCategory category = PlanCategory.builder()
                .userId(userId)
                .name(name)
                .color(request.color())
                .icon(request.icon())
                .isDefault(Boolean.TRUE.equals(request.isDefault()))
                .build();

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    public PlanCategoryResponse getCategory(UUID userId, UUID categoryId) {
        return categoryMapper.toResponse(getCategoryEntity(userId, categoryId));
    }

    @Transactional(readOnly = true)
    public Page<PlanCategoryResponse> listCategories(UUID userId, Pageable pageable) {
        return categoryRepository.findByUserIdAndDeletedAtIsNull(userId, pageable)
                .map(categoryMapper::toResponse);
    }

    @Transactional
    public PlanCategoryResponse updateCategory(UUID userId, UUID categoryId, UpdatePlanCategoryRequest request) {
        PlanCategory category = getCategoryEntity(userId, categoryId);
        String name = request.name().trim();

        if (!name.equals(category.getName())
                && categoryRepository.existsByUserIdAndNameAndDeletedAtIsNull(userId, name)) {
            throw new ConflictException("Category name already exists");
        }

        category.updateDetails(
                name,
                request.color(),
                request.icon(),
                Boolean.TRUE.equals(request.isDefault())
        );

        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void deleteCategory(UUID userId, UUID categoryId) {
        PlanCategory category = getCategoryEntity(userId, categoryId);
        category.softDelete(userId, null);
    }

    private PlanCategory getCategoryEntity(UUID userId, UUID categoryId) {
        return categoryRepository.findByIdAndUserIdAndDeletedAtIsNull(categoryId, userId)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }
}

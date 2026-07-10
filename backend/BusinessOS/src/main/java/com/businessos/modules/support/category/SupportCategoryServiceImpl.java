package com.businessos.modules.support.category;

import com.businessos.shared.exception.ResourceNotFoundException;
import com.businessos.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportCategoryServiceImpl implements SupportCategoryService {

    private final SupportCategoryRepository categoryRepository;

    @Override
    @Transactional
    public SupportCategoryResponse create(SupportCategoryRequest request) {
        if (categoryRepository.findByCategoryName(request.getCategoryName()).isPresent()) {
            throw new BadRequestException("Category name already exists: " + request.getCategoryName());
        }

        SupportCategory category = SupportCategory.builder()
                .categoryName(request.getCategoryName())
                .description(request.getDescription())
                .icon(request.getIcon())
                .active(true)
                .build();

        category = categoryRepository.save(category);
        return SupportCategoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportCategoryResponse getById(Long id) {
        SupportCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return SupportCategoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public SupportCategoryResponse getByName(String name) {
        SupportCategory category = categoryRepository.findByCategoryName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return SupportCategoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupportCategoryResponse> getAll(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(SupportCategoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportCategoryResponse> getActive() {
        return categoryRepository.findByActiveTrue()
                .stream()
                .map(SupportCategoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupportCategoryResponse update(Long id, SupportCategoryRequest request) {
        SupportCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getCategoryName().equals(request.getCategoryName())) {
            if (categoryRepository.findByCategoryName(request.getCategoryName()).isPresent()) {
                throw new BadRequestException("Category name already exists");
            }
        }

        category.setCategoryName(request.getCategoryName());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());

        category = categoryRepository.save(category);
        return SupportCategoryMapper.toResponse(category);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, boolean active) {
        SupportCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setActive(active);
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public SupportCategoryResponse delete(Long id) {
        SupportCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.softDelete();
        categoryRepository.save(category);
        return SupportCategoryMapper.toResponse(category);
    }
}

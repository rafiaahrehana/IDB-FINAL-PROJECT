package com.businessos.modules.servicedesk.kb;

import com.businessos.auth.role.enums.Role;
import com.businessos.auth.user.User;
import com.businessos.modules.servicedesk.servicecategory.ServiceCategory;
import com.businessos.modules.servicedesk.servicecategory.ServiceCategoryRepository;
import com.businessos.modules.servicedesk.servicetemplate.ServiceTemplate;
import com.businessos.modules.servicedesk.servicetemplate.ServiceTemplateRepository;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KbArticleServiceImpl implements KbArticleService {

    private final KbArticleRepository articleRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final ServiceTemplateRepository templateRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public KbArticleResponse create(KbArticleRequest request) {
        KbArticle article = KbArticle.builder()
                .title(request.getTitle())
                .summary(request.getSummary())
                .content(request.getContent())
                .clientVisible(request.isClientVisible())
                .keywords(request.getKeywords())
                .category(resolveCategory(request.getCategoryId()))
                .relatedService(resolveRelatedService(request.getRelatedServiceId()))
                .author(securityUtil.getCurrentUser())
                .build();

        return KbArticleMapper.toResponse(articleRepository.save(article));
    }

    @Override
    @Transactional
    public KbArticleResponse update(Long id, KbArticleRequest request) {
        KbArticle article = getByIdOrThrow(id);

        article.setTitle(request.getTitle());
        article.setSummary(request.getSummary());
        article.setContent(request.getContent());
        article.setClientVisible(request.isClientVisible());
        article.setKeywords(request.getKeywords());
        article.setCategory(resolveCategory(request.getCategoryId()));
        article.setRelatedService(resolveRelatedService(request.getRelatedServiceId()));

        return KbArticleMapper.toResponse(articleRepository.save(article));
    }

    @Override
    @Transactional
    public KbArticleResponse getById(Long id) {
        KbArticle article = getByIdOrThrow(id);

        if (isClient() && !isVisibleToClient(article)) {
            throw new ResourceNotFoundException("Article not found");
        }

        return KbArticleMapper.toResponse(article);
    }

    @Override
    public Page<KbArticleResponse> list(String keyword, String status, Pageable pageable) {
        KbArticleStatus statusFilter;
        try {
            statusFilter = (status == null || status.isBlank()) ? null : KbArticleStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid article status: '" + status + "'. Valid values: DRAFT, PUBLISHED, ARCHIVED");
        }
        String keywordFilter = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        return articleRepository.search(keywordFilter, statusFilter, isClient(), pageable)
                .map(KbArticleMapper::toResponse);
    }

    @Override
    @Transactional
    public KbArticleResponse publish(Long id) {
        KbArticle article = getByIdOrThrow(id);
        article.setStatus(KbArticleStatus.PUBLISHED);
        article.setPublishedAt(LocalDateTime.now());
        return KbArticleMapper.toResponse(articleRepository.save(article));
    }

    @Override
    @Transactional
    public KbArticleResponse archive(Long id) {
        KbArticle article = getByIdOrThrow(id);
        article.setStatus(KbArticleStatus.ARCHIVED);
        return KbArticleMapper.toResponse(articleRepository.save(article));
    }

    @Override
    @Transactional
    public KbArticleResponse markHelpful(Long id) {
        KbArticle article = getByIdOrThrow(id);

        if (isClient() && !isVisibleToClient(article)) {
            throw new ResourceNotFoundException("Article not found");
        }

        article.setHelpfulCount(article.getHelpfulCount() + 1);
        return KbArticleMapper.toResponse(articleRepository.save(article));
    }

    private KbArticle getByIdOrThrow(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found"));
    }

    private ServiceCategory resolveCategory(Long categoryId) {
        if (categoryId == null) return null;
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private ServiceTemplate resolveRelatedService(Long relatedServiceId) {
        if (relatedServiceId == null) return null;
        return templateRepository.findById(relatedServiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Related service not found"));
    }

    private boolean isVisibleToClient(KbArticle article) {
        return article.getStatus() == KbArticleStatus.PUBLISHED && article.isClientVisible();
    }

    private boolean isClient() {
        User user = securityUtil.getCurrentUser();
        return user != null && user.getRole() == Role.CLIENT;
    }
}

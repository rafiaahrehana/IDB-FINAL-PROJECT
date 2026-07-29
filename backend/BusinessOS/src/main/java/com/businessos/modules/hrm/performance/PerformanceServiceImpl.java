package com.businessos.modules.hrm.performance;

import com.businessos.modules.ai.enums.AiFeature;
import com.businessos.modules.ai.prompt.PerformanceReviewPromptBuilder;
import com.businessos.modules.ai.service.AiService;
import com.businessos.modules.hrm.employee.Employee;
import com.businessos.modules.hrm.employee.EmployeeRepository;
import com.businessos.modules.company.Company;
import com.businessos.modules.company.CompanyRepository;
import com.businessos.auth.role.enums.PermissionCode;
import com.businessos.auth.role.service.AuthorizationService;
import com.businessos.security.SecurityUtil;
import com.businessos.shared.email.EmailBranding;
import com.businessos.shared.email.EmailService;
import com.businessos.shared.exception.BadRequestException;
import com.businessos.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceReviewRepository reviewRepository;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;
    private final EmailBranding emailBranding;
    private final AuthorizationService authorizationService;
    private final AiService aiService;

    @Override
    @Transactional
    public PerformanceReviewResponse create(PerformanceReviewRequest request) {
        authorizationService.checkPermission(PermissionCode.PERFORMANCE_CREATE);
        Long companyId = requireCompanyId();
        Employee employee = employeeRepository.findByIdAndCompanyId(request.getEmployeeId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + request.getEmployeeId()));
        Employee reviewer = employeeRepository.findByUserId(securityUtil.getCurrentUser().getId())
                .orElseThrow(() -> new BadRequestException("Employee profile not found"));

        PerformanceReview review = PerformanceReview.builder()
                .employee(employee)
                .company(companyRef(companyId))
                .reviewedBy(reviewer)
                .reviewPeriodStart(request.getReviewPeriodStart())
                .reviewPeriodEnd(request.getReviewPeriodEnd())
                .scoreWorkQuality(request.getScoreWorkQuality())
                .scoreProductivity(request.getScoreProductivity())
                .scoreCommunication(request.getScoreCommunication())
                .scoreTeamwork(request.getScoreTeamwork())
                .scoreInitiative(request.getScoreInitiative())
                .scorePunctuality(request.getScorePunctuality())
                .overallScore(calculateOverall(
                        request.getScoreWorkQuality(), request.getScoreProductivity(),
                        request.getScoreCommunication(), request.getScoreTeamwork(),
                        request.getScoreInitiative(), request.getScorePunctuality()
                ))
                .strengths(request.getStrengths())
                .areasForImprovement(request.getAreasForImprovement())
                .goalsForNextPeriod(request.getGoalsForNextPeriod())
                .comments(request.getComments())
                .build();

        reviewRepository.save(review);
        
        if (employee.getUser() != null) {
            try {
                Company fullCompany = companyRepository.findById(companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found"));
                EmailBranding.Data branding = emailBranding.from(fullCompany);
                emailService.sendPerformanceReviewEmail(employee.getUser().getEmail(), employee.getUser().getFirstName(), branding);
                
            } catch (Exception ex) {
                // Best-effort notification — a failed email must not roll back the review.
                log.warn("Performance review email failed (review still saved): {}", ex.getMessage());
            }
        }

        return PerformanceMapper.toPerformanceReviewResponse(review);
    }

    @Override
    @Transactional(readOnly = true)
    public PerformanceReviewResponse getById(Long id) {
        return PerformanceMapper.toPerformanceReviewResponse(findInTenant(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerformanceReviewResponse> listAll(Pageable pageable) {
        authorizationService.checkPermission(PermissionCode.PERFORMANCE_VIEW);
        return reviewRepository.findByCompanyId(requireCompanyId(), pageable)
                .map(PerformanceMapper::toPerformanceReviewResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PerformanceReviewResponse> listForEmployee(Long employeeId, Pageable pageable) {
        return reviewRepository.findByCompanyIdAndEmployeeId(requireCompanyId(), employeeId, pageable)
                .map(PerformanceMapper::toPerformanceReviewResponse);
    }

    @Override
    @Transactional
    public PerformanceReviewResponse update(Long id, PerformanceReviewRequest request) {
        authorizationService.checkPermission(PermissionCode.PERFORMANCE_UPDATE);
        PerformanceReview review = findInTenant(id);
        if (review.isFinalised()) throw new BadRequestException("Cannot edit a finalised review");

        if (request.getScoreWorkQuality() != null)    review.setScoreWorkQuality(request.getScoreWorkQuality());
        if (request.getScoreProductivity() != null)   review.setScoreProductivity(request.getScoreProductivity());
        if (request.getScoreCommunication() != null)  review.setScoreCommunication(request.getScoreCommunication());
        if (request.getScoreTeamwork() != null)       review.setScoreTeamwork(request.getScoreTeamwork());
        if (request.getScoreInitiative() != null)     review.setScoreInitiative(request.getScoreInitiative());
        if (request.getScorePunctuality() != null)    review.setScorePunctuality(request.getScorePunctuality());
        if (request.getStrengths() != null)           review.setStrengths(request.getStrengths());
        if (request.getAreasForImprovement() != null) review.setAreasForImprovement(request.getAreasForImprovement());
        if (request.getGoalsForNextPeriod() != null)  review.setGoalsForNextPeriod(request.getGoalsForNextPeriod());
        if (request.getComments() != null)            review.setComments(request.getComments());

        review.setOverallScore(calculateOverall(
                review.getScoreWorkQuality(), review.getScoreProductivity(),
                review.getScoreCommunication(), review.getScoreTeamwork(),
                review.getScoreInitiative(), review.getScorePunctuality()
        ));

        reviewRepository.save(review);
        return PerformanceMapper.toPerformanceReviewResponse(review);
    }

    @Override
    @Transactional
    public PerformanceReviewResponse finalise(Long id) {
        authorizationService.checkPermission(PermissionCode.PERFORMANCE_UPDATE);
        PerformanceReview review = findInTenant(id);
        if (review.isFinalised()) throw new BadRequestException("Review is already finalised");
        review.setFinalised(true);
        return PerformanceMapper.toPerformanceReviewResponse(review);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        authorizationService.checkPermission(PermissionCode.PERFORMANCE_DELETE);
        PerformanceReview review = findInTenant(id);
        if (review.isFinalised()) throw new BadRequestException("Cannot delete a finalised review");
        review.softDelete();
    }

    @Override
    @Transactional(readOnly = true)
    public PerformanceReviewResponse summarise(Long id) {
        authorizationService.checkPermission(PermissionCode.PERFORMANCE_VIEW);
        PerformanceReview review = findInTenant(id);
        PerformanceReviewResponse response = PerformanceMapper.toPerformanceReviewResponse(review);

        if (review.getOverallScore() == null) {
            throw new BadRequestException("Fill in at least one KPI score before generating an AI summary");
        }
        Employee employee = review.getEmployee();

        String prompt = PerformanceReviewPromptBuilder.builder()
            .setEmployeeName(employee.getUser().getFullName())
            .setDesignation(employee.getDesignation() != null ? employee.getDesignation().getName() : employee.getJobTitle())
            .setReviewPeriod(review.getReviewPeriodStart() + " to " + review.getReviewPeriodEnd())
            .setOverallScore((int) Math.round(review.getOverallScore()))
            .setStrengths(review.getStrengths())
            .setAreasForImprovement(review.getAreasForImprovement())
            .setGoalsForNextPeriod(review.getGoalsForNextPeriod())
            .build();

        response.setAiSummary(aiService.generateRaw(AiFeature.PERFORMANCE_REVIEW, prompt));
        return response;
    }

    private PerformanceReview findInTenant(Long id) {
        return reviewRepository.findByIdAndCompanyId(id, requireCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Performance review not found: " + id));
    }


    private Double calculateOverall(Integer... scores) {
        List<Integer> present = Stream.of(scores)
                .filter(Objects::nonNull)
                .toList();
        if (present.isEmpty()) return null;
        double avg = present.stream().mapToInt(Integer::intValue).average().orElse(0);
        return Math.round(avg * 10.0) / 10.0;
    }

    private Long requireCompanyId() {
        Long id = securityUtil.getCurrentCompanyId();
        if (id == null) throw new BadRequestException("No company context");
        return id;
    }

    private Company companyRef(Long companyId) {
        Company c = new Company();
        c.setId(companyId);
        return c;
    }
}
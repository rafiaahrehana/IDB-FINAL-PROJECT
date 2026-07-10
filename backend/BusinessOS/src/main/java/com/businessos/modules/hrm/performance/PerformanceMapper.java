package com.businessos.modules.hrm.performance;

import com.businessos.auth.user.User;
import com.businessos.modules.hrm.employee.Employee;

public class PerformanceMapper {

    public static PerformanceReviewResponse toPerformanceReviewResponse(PerformanceReview pr) {
        Employee emp = pr.getEmployee();
        User empUser = emp != null ? emp.getUser() : null;
        Employee reviewer = pr.getReviewedBy();
        User reviewerUser = reviewer != null ? reviewer.getUser() : null;
        PerformanceReviewResponse r = new PerformanceReviewResponse();
        r.setId(pr.getId());
        r.setReviewPeriodStart(pr.getReviewPeriodStart());
        r.setReviewPeriodEnd(pr.getReviewPeriodEnd());
        r.setScoreWorkQuality(pr.getScoreWorkQuality());
        r.setScoreProductivity(pr.getScoreProductivity());
        r.setScoreCommunication(pr.getScoreCommunication());
        r.setScoreTeamwork(pr.getScoreTeamwork());
        r.setScoreInitiative(pr.getScoreInitiative());
        r.setScorePunctuality(pr.getScorePunctuality());
        r.setOverallScore(pr.getOverallScore());
        r.setStrengths(pr.getStrengths());
        r.setAreasForImprovement(pr.getAreasForImprovement());
        r.setGoalsForNextPeriod(pr.getGoalsForNextPeriod());
        r.setComments(pr.getComments());
        r.setFinalised(pr.isFinalised());
        r.setEmployeeId(emp != null ? emp.getId() : null);
        r.setEmployeeName(empUser != null ? empUser.getFullName() : null);
        r.setReviewedById(reviewer != null ? reviewer.getId() : null);
        r.setReviewedByName(reviewerUser != null ? reviewerUser.getFullName() : null);
        r.setCreatedAt(pr.getCreatedAt());
        return r;

    }
}

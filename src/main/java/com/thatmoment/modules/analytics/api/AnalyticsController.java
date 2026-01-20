package com.thatmoment.modules.analytics.api;

import com.thatmoment.common.constants.ApiDescriptions;
import com.thatmoment.modules.analytics.dto.response.AnalyticsSummaryResponse;
import com.thatmoment.modules.analytics.dto.response.PlanCompletionTrendResponse;
import com.thatmoment.modules.analytics.dto.response.RoutineCompletionTrendResponse;
import com.thatmoment.modules.analytics.service.AnalyticsService;
import com.thatmoment.modules.auth.security.UserPrincipal;
import com.thatmoment.modules.journal.domain.enums.MoodType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name = ApiDescriptions.TAG_ANALYTICS, description = ApiDescriptions.TAG_ANALYTICS_DESC)
@PreAuthorize("isAuthenticated()")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary/weekly")
    @Operation(summary = ApiDescriptions.ANALYTICS_WEEKLY_SUMMARY)
    public AnalyticsSummaryResponse getWeeklySummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) LocalDate week
    ) {
        return analyticsService.getWeeklySummary(principal.getUserId(), week);
    }

    @GetMapping("/summary/monthly")
    @Operation(summary = ApiDescriptions.ANALYTICS_MONTHLY_SUMMARY)
    public AnalyticsSummaryResponse getMonthlySummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        return analyticsService.getMonthlySummary(principal.getUserId(), month);
    }

    @GetMapping("/summary/yearly")
    @Operation(summary = ApiDescriptions.ANALYTICS_YEARLY_SUMMARY)
    public AnalyticsSummaryResponse getYearlySummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Integer year
    ) {
        return analyticsService.getYearlySummary(principal.getUserId(), year);
    }

    @GetMapping("/plans/completion")
    @Operation(summary = ApiDescriptions.ANALYTICS_PLAN_COMPLETION_SUMMARY)
    public List<PlanCompletionTrendResponse> getPlanCompletionTrend(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return analyticsService.getPlanCompletionTrend(principal.getUserId(), from, to);
    }

    @GetMapping("/journals/moods")
    @Operation(summary = ApiDescriptions.ANALYTICS_JOURNAL_MOOD_SUMMARY)
    public Map<MoodType, Long> getJournalMoodDistribution(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return analyticsService.getJournalMoodDistribution(principal.getUserId(), from, to);
    }

    @GetMapping("/routines/completion")
    @Operation(summary = ApiDescriptions.ANALYTICS_ROUTINE_COMPLETION_SUMMARY)
    public List<RoutineCompletionTrendResponse> getRoutineCompletionTrend(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return analyticsService.getRoutineCompletionTrend(principal.getUserId(), from, to);
    }
}

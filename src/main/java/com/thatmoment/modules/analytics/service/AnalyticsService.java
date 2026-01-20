package com.thatmoment.modules.analytics.service;

import com.thatmoment.modules.analytics.dto.response.AnalyticsSummaryResponse;
import com.thatmoment.modules.analytics.dto.response.PlanCompletionTrendResponse;
import com.thatmoment.modules.analytics.dto.response.RoutineCompletionTrendResponse;
import com.thatmoment.modules.journal.domain.enums.MoodType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface AnalyticsService {

    AnalyticsSummaryResponse getWeeklySummary(UUID userId, LocalDate week);

    AnalyticsSummaryResponse getMonthlySummary(UUID userId, YearMonth month);

    AnalyticsSummaryResponse getYearlySummary(UUID userId, Integer year);

    List<PlanCompletionTrendResponse> getPlanCompletionTrend(UUID userId, LocalDate from, LocalDate to);

    Map<MoodType, Long> getJournalMoodDistribution(UUID userId, LocalDate from, LocalDate to);

    List<RoutineCompletionTrendResponse> getRoutineCompletionTrend(UUID userId, LocalDate from, LocalDate to);
}

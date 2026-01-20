package com.thatmoment.modules.analytics.service.impl;

import com.thatmoment.common.exception.exceptions.BadRequestException;
import com.thatmoment.modules.analytics.constants.AnalyticsMessages;
import com.thatmoment.modules.analytics.dto.response.AnalyticsJournalsSummaryResponse;
import com.thatmoment.modules.analytics.dto.response.AnalyticsPeriodResponse;
import com.thatmoment.modules.analytics.dto.response.AnalyticsPlansSummaryResponse;
import com.thatmoment.modules.analytics.dto.response.AnalyticsRoutinesSummaryResponse;
import com.thatmoment.modules.analytics.dto.response.AnalyticsSummaryResponse;
import com.thatmoment.modules.analytics.dto.response.PlanCompletionTrendResponse;
import com.thatmoment.modules.analytics.dto.response.RoutineCompletionTrendResponse;
import com.thatmoment.modules.analytics.service.AnalyticsService;
import com.thatmoment.modules.journal.domain.enums.MoodType;
import com.thatmoment.modules.journal.repository.JournalEntryRepository;
import com.thatmoment.modules.plan.repository.PlanRepository;
import com.thatmoment.modules.profile.domain.enums.WeekStartDay;
import com.thatmoment.modules.profile.dto.response.UserPreferencesResponse;
import com.thatmoment.modules.profile.service.UserPreferencesService;
import com.thatmoment.modules.routine.domain.enums.ProgressStatus;
import com.thatmoment.modules.routine.repository.RoutineProgressRepository;
import com.thatmoment.modules.routine.repository.RoutineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
class AnalyticsServiceImpl implements AnalyticsService {

    private final PlanRepository planRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final RoutineRepository routineRepository;
    private final RoutineProgressRepository routineProgressRepository;
    private final UserPreferencesService userPreferencesService;

    AnalyticsServiceImpl(
            PlanRepository planRepository,
            JournalEntryRepository journalEntryRepository,
            RoutineRepository routineRepository,
            RoutineProgressRepository routineProgressRepository,
            UserPreferencesService userPreferencesService
    ) {
        this.planRepository = planRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.routineRepository = routineRepository;
        this.routineProgressRepository = routineProgressRepository;
        this.userPreferencesService = userPreferencesService;
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getWeeklySummary(UUID userId, LocalDate week) {
        UserPreferencesResponse preferences = userPreferencesService.getPreferences(userId);
        LocalDate baseDate = resolveUserDate(preferences, week);
        LocalDate[] range = resolveWeekRange(preferences, baseDate);
        return buildSummary(userId, preferences, range[0], range[1]);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getMonthlySummary(UUID userId, YearMonth month) {
        UserPreferencesResponse preferences = userPreferencesService.getPreferences(userId);
        LocalDate baseDate = resolveUserDate(preferences, null);
        YearMonth resolvedMonth = month != null ? month : YearMonth.from(baseDate);
        LocalDate from = resolvedMonth.atDay(1);
        LocalDate to = resolvedMonth.atEndOfMonth();
        return buildSummary(userId, preferences, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getYearlySummary(UUID userId, Integer year) {
        UserPreferencesResponse preferences = userPreferencesService.getPreferences(userId);
        LocalDate baseDate = resolveUserDate(preferences, null);
        int resolvedYear = year != null ? year : baseDate.getYear();
        LocalDate from = LocalDate.of(resolvedYear, 1, 1);
        LocalDate to = LocalDate.of(resolvedYear, 12, 31);
        return buildSummary(userId, preferences, from, to);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanCompletionTrendResponse> getPlanCompletionTrend(UUID userId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        Map<LocalDate, Long> totalMap = toDateCountMap(
                planRepository.countTotalsByDate(userId, from, to)
        );
        Map<LocalDate, Long> completedMap = toDateCountMap(
                planRepository.countCompletedByDate(userId, from, to)
        );
        List<PlanCompletionTrendResponse> responses = new ArrayList<>();
        for (LocalDate date : buildDateRange(from, to)) {
            responses.add(new PlanCompletionTrendResponse(
                    date,
                    completedMap.getOrDefault(date, 0L),
                    totalMap.getOrDefault(date, 0L)
            ));
        }
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<MoodType, Long> getJournalMoodDistribution(UUID userId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        Map<MoodType, Long> counts = initializeMoodCounts();
        for (Object[] row : journalEntryRepository.countMoodsByDateRange(userId, from, to)) {
            MoodType mood = (MoodType) row[0];
            Number count = (Number) row[1];
            if (mood != null) {
                counts.put(mood, count.longValue());
            }
        }
        return counts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineCompletionTrendResponse> getRoutineCompletionTrend(UUID userId, LocalDate from, LocalDate to) {
        validateRange(from, to);
        Map<LocalDate, Long> completedMap = toDateCountMap(
                routineProgressRepository.countCompletedByDate(userId, from, to, ProgressStatus.COMPLETED)
        );
        List<RoutineCompletionTrendResponse> responses = new ArrayList<>();
        for (LocalDate date : buildDateRange(from, to)) {
            responses.add(new RoutineCompletionTrendResponse(
                    date,
                    completedMap.getOrDefault(date, 0L)
            ));
        }
        return responses;
    }

    private AnalyticsSummaryResponse buildSummary(
            UUID userId,
            UserPreferencesResponse preferences,
            LocalDate from,
            LocalDate to
    ) {
        long planTotal = planRepository.countByUserIdAndPlanDateBetweenAndDeletedAtIsNull(userId, from, to);
        long planCompleted = planRepository.countByUserIdAndPlanDateBetweenAndIsCompletedTrueAndDeletedAtIsNull(
                userId,
                from,
                to
        );
        long journalEntries = journalEntryRepository.countByUserIdAndEntryDateBetweenAndDeletedAtIsNull(
                userId,
                from,
                to
        );
        Map<MoodType, Long> moodCounts = initializeMoodCounts();
        for (Object[] row : journalEntryRepository.countMoodsByDateRange(userId, from, to)) {
            MoodType mood = (MoodType) row[0];
            Number count = (Number) row[1];
            if (mood != null) {
                moodCounts.put(mood, count.longValue());
            }
        }
        long activeRoutines = routineRepository.countByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId);
        long completedDays = routineProgressRepository.countByUserIdAndProgressDateBetweenAndStatus(
                userId,
                from,
                to,
                ProgressStatus.COMPLETED
        );

        AnalyticsPeriodResponse period = new AnalyticsPeriodResponse(from, to, resolveWeekStart(preferences));
        AnalyticsPlansSummaryResponse plans = new AnalyticsPlansSummaryResponse(planTotal, planCompleted);
        AnalyticsJournalsSummaryResponse journals = new AnalyticsJournalsSummaryResponse(journalEntries, moodCounts);
        AnalyticsRoutinesSummaryResponse routines = new AnalyticsRoutinesSummaryResponse(activeRoutines, completedDays);
        return new AnalyticsSummaryResponse(period, plans, journals, routines);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new BadRequestException(AnalyticsMessages.INVALID_DATE_RANGE);
        }
    }

    private Map<LocalDate, Long> toDateCountMap(List<Object[]> results) {
        Map<LocalDate, Long> counts = new HashMap<>();
        for (Object[] row : results) {
            LocalDate date = (LocalDate) row[0];
            Number count = (Number) row[1];
            counts.put(date, count.longValue());
        }
        return counts;
    }

    private Map<MoodType, Long> initializeMoodCounts() {
        Map<MoodType, Long> counts = new EnumMap<>(MoodType.class);
        for (MoodType mood : MoodType.values()) {
            counts.put(mood, 0L);
        }
        return counts;
    }

    private List<LocalDate> buildDateRange(LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = from;
        while (!current.isAfter(to)) {
            dates.add(current);
            current = current.plusDays(1);
        }
        return dates;
    }

    private LocalDate resolveUserDate(UserPreferencesResponse preferences, LocalDate date) {
        if (date != null) {
            return date;
        }
        String timezone = preferences.timezone();
        if (timezone == null || timezone.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.now(ZoneId.of(timezone));
        } catch (DateTimeException exception) {
            return LocalDate.now();
        }
    }

    private LocalDate[] resolveWeekRange(UserPreferencesResponse preferences, LocalDate date) {
        WeekStartDay weekStartDay = preferences.weekStartDay();
        DayOfWeek startOfWeek = weekStartDay == WeekStartDay.SUNDAY
                ? DayOfWeek.SUNDAY
                : DayOfWeek.MONDAY;
        int offset = (date.getDayOfWeek().getValue() - startOfWeek.getValue() + 7) % 7;
        LocalDate from = date.minusDays(offset);
        LocalDate to = from.plusDays(6);
        return new LocalDate[]{from, to};
    }

    private String resolveWeekStart(UserPreferencesResponse preferences) {
        WeekStartDay weekStartDay = preferences.weekStartDay();
        return weekStartDay == WeekStartDay.SUNDAY ? "SUN" : "MON";
    }
}

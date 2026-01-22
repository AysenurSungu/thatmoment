package com.thatmoment.modules.routine.service.impl;

import com.thatmoment.common.dto.MessageResponse;
import com.thatmoment.common.exception.exceptions.BadRequestException;
import com.thatmoment.common.exception.exceptions.NotFoundException;
import com.thatmoment.modules.profile.domain.enums.WeekStartDay;
import com.thatmoment.modules.profile.dto.response.UserPreferencesResponse;
import com.thatmoment.modules.profile.service.UserPreferencesService;
import com.thatmoment.modules.routine.constants.RoutineMessages;
import com.thatmoment.modules.routine.domain.Routine;
import com.thatmoment.modules.routine.domain.RoutineProgress;
import com.thatmoment.modules.routine.domain.RoutineReminder;
import com.thatmoment.modules.routine.domain.RoutineSchedule;
import com.thatmoment.modules.routine.domain.enums.ProgressStatus;
import com.thatmoment.modules.routine.domain.enums.RoutineDayOfWeek;
import com.thatmoment.modules.routine.domain.enums.RoutineType;
import com.thatmoment.modules.routine.dto.request.CreateRoutineProgressRequest;
import com.thatmoment.modules.routine.dto.request.CreateRoutineRequest;
import com.thatmoment.modules.routine.dto.request.SkipRoutineRequest;
import com.thatmoment.modules.routine.dto.request.UpdateRoutineProgressRequest;
import com.thatmoment.modules.routine.dto.request.UpdateRoutineRequest;
import com.thatmoment.modules.routine.dto.request.UpdateRoutineRemindersRequest;
import com.thatmoment.modules.routine.dto.response.RoutineOverviewResponse;
import com.thatmoment.modules.routine.dto.response.RoutineProgressResponse;
import com.thatmoment.modules.routine.dto.response.RoutineRemindersResponse;
import com.thatmoment.modules.routine.dto.response.RoutineResponse;
import com.thatmoment.modules.routine.dto.response.RoutineScheduleResponse;
import com.thatmoment.modules.routine.dto.response.RoutineSummaryResponse;
import com.thatmoment.modules.routine.mapper.RoutineMapper;
import com.thatmoment.modules.routine.mapper.RoutineProgressMapper;
import com.thatmoment.modules.routine.repository.RoutineProgressRepository;
import com.thatmoment.modules.routine.repository.RoutineRepository;
import com.thatmoment.modules.routine.repository.RoutineReminderRepository;
import com.thatmoment.modules.routine.repository.RoutineScheduleRepository;
import com.thatmoment.modules.routine.service.RoutineService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
class RoutineServiceImpl implements RoutineService {

    private final RoutineRepository routineRepository;
    private final RoutineScheduleRepository scheduleRepository;
    private final RoutineProgressRepository progressRepository;
    private final RoutineReminderRepository reminderRepository;
    private final RoutineMapper routineMapper;
    private final RoutineProgressMapper progressMapper;
    private final UserPreferencesService userPreferencesService;

    RoutineServiceImpl(
            RoutineRepository routineRepository,
            RoutineScheduleRepository scheduleRepository,
            RoutineProgressRepository progressRepository,
            RoutineReminderRepository reminderRepository,
            RoutineMapper routineMapper,
            RoutineProgressMapper progressMapper,
            UserPreferencesService userPreferencesService
    ) {
        this.routineRepository = routineRepository;
        this.scheduleRepository = scheduleRepository;
        this.progressRepository = progressRepository;
        this.reminderRepository = reminderRepository;
        this.routineMapper = routineMapper;
        this.progressMapper = progressMapper;
        this.userPreferencesService = userPreferencesService;
    }

    @Override
    @Transactional
    public RoutineResponse createRoutine(UUID userId, CreateRoutineRequest request) {
        Routine routine = Routine.builder()
                .userId(userId)
                .title(request.title())
                .description(request.description())
                .type(request.type())
                .targetValue(request.targetValue())
                .unit(request.unit())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .isActive(request.isActive())
                .build();

        Routine savedRoutine = routineRepository.save(routine);
        List<RoutineDayOfWeek> daysOfWeek = normalizeDays(request.schedule().daysOfWeek());
        saveSchedule(savedRoutine.getId(), daysOfWeek);

        return toRoutineResponse(savedRoutine, daysOfWeek);
    }

    @Override
    @Transactional(readOnly = true)
    public RoutineResponse getRoutine(UUID userId, UUID routineId) {
        Routine routine = getRoutineEntity(userId, routineId);
        List<RoutineDayOfWeek> daysOfWeek = getDaysOfWeek(routineId);
        return toRoutineResponse(routine, daysOfWeek);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoutineResponse> listRoutines(UUID userId, String status, String query, Pageable pageable) {
        Boolean isActive = parseStatus(status);
        Page<Routine> routines = routineRepository.search(userId, isActive, normalizeQuery(query), pageable);
        List<RoutineSchedule> schedules = routines.isEmpty()
                ? List.of()
                : scheduleRepository.findByRoutineIdIn(routines.stream().map(Routine::getId).toList());
        Map<UUID, List<RoutineDayOfWeek>> scheduleMap = mapSchedules(schedules);

        return routines.map(routine -> toRoutineResponse(
                routine,
                scheduleMap.getOrDefault(routine.getId(), List.of())
        ));
    }

    @Override
    @Transactional
    public RoutineResponse updateRoutine(UUID userId, UUID routineId, UpdateRoutineRequest request) {
        Routine routine = getRoutineEntity(userId, routineId);
        routine.updateDetails(
                request.title(),
                request.description(),
                request.type(),
                request.targetValue(),
                request.unit(),
                request.startDate(),
                request.endDate(),
                request.isActive()
        );

        List<RoutineDayOfWeek> daysOfWeek = normalizeDays(request.schedule().daysOfWeek());
        scheduleRepository.deleteByRoutineId(routineId);
        saveSchedule(routineId, daysOfWeek);

        return toRoutineResponse(routine, daysOfWeek);
    }

    @Override
    @Transactional
    public void deleteRoutine(UUID userId, UUID routineId) {
        Routine routine = getRoutineEntity(userId, routineId);
        routine.softDelete(userId, null);
    }

    @Override
    @Transactional
    public MessageResponse activateRoutine(UUID userId, UUID routineId) {
        Routine routine = getRoutineEntity(userId, routineId);
        routine.activate();
        return MessageResponse.of(RoutineMessages.ROUTINE_ACTIVATED);
    }

    @Override
    @Transactional
    public MessageResponse deactivateRoutine(UUID userId, UUID routineId) {
        Routine routine = getRoutineEntity(userId, routineId);
        routine.deactivate();
        return MessageResponse.of(RoutineMessages.ROUTINE_DEACTIVATED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineResponse> listTodayRoutines(UUID userId) {
        LocalDate today = resolveUserDate(userId, null);
        RoutineDayOfWeek dayOfWeek = RoutineDayOfWeek.from(today.getDayOfWeek());
        List<Routine> routines = routineRepository.findTodayRoutines(userId, today, dayOfWeek);
        List<RoutineSchedule> schedules = routines.isEmpty()
                ? List.of()
                : scheduleRepository.findByRoutineIdIn(routines.stream().map(Routine::getId).toList());
        Map<UUID, List<RoutineDayOfWeek>> scheduleMap = mapSchedules(schedules);

        return routines.stream()
                .map(routine -> toRoutineResponse(
                        routine,
                        scheduleMap.getOrDefault(routine.getId(), List.of())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineResponse> listActiveRoutines(UUID userId) {
        List<Routine> routines = routineRepository.findByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId);
        List<RoutineSchedule> schedules = routines.isEmpty()
                ? List.of()
                : scheduleRepository.findByRoutineIdIn(routines.stream().map(Routine::getId).toList());
        Map<UUID, List<RoutineDayOfWeek>> scheduleMap = mapSchedules(schedules);

        return routines.stream()
                .map(routine -> toRoutineResponse(
                        routine,
                        scheduleMap.getOrDefault(routine.getId(), List.of())
                ))
                .toList();
    }

    @Override
    @Transactional
    public RoutineProgressResponse addProgress(
            UUID userId,
            UUID routineId,
            CreateRoutineProgressRequest request
    ) {
        Routine routine = getRoutineEntity(userId, routineId);
        assertRoutineActive(routine);
        LocalDate date = request.date();
        validateProgressDate(routine, date);
        validateSchedule(routineId, date);
        validateProgressAmount(routine, request.amount());

        progressRepository.findByRoutineIdAndProgressDate(routineId, date)
                .ifPresent(existing -> {
                    throw new BadRequestException(RoutineMessages.ROUTINE_PROGRESS_EXISTS);
                });

        RoutineProgress progress = RoutineProgress.builder()
                .routineId(routineId)
                .userId(userId)
                .progressDate(date)
                .amount(request.amount())
                .status(resolveStatus(routine, request.amount()))
                .build();

        return progressMapper.toResponse(progressRepository.save(progress));
    }

    @Override
    @Transactional
    public RoutineProgressResponse updateProgress(
            UUID userId,
            UUID routineId,
            LocalDate date,
            UpdateRoutineProgressRequest request
    ) {
        Routine routine = getRoutineEntity(userId, routineId);
        assertRoutineActive(routine);
        validateProgressDate(routine, date);
        validateSchedule(routineId, date);
        validateProgressAmount(routine, request.amount());

        RoutineProgress progress = progressRepository.findByRoutineIdAndProgressDate(routineId, date)
                .orElseThrow(() -> new NotFoundException(RoutineMessages.ROUTINE_PROGRESS_NOT_FOUND));

        progress.updateProgress(request.amount(), resolveStatus(routine, request.amount()));

        return progressMapper.toResponse(progress);
    }

    @Override
    @Transactional
    public RoutineProgressResponse skipRoutine(UUID userId, UUID routineId, SkipRoutineRequest request) {
        Routine routine = getRoutineEntity(userId, routineId);
        assertRoutineActive(routine);
        LocalDate date = request.date();
        validateProgressDate(routine, date);
        validateSchedule(routineId, date);

        RoutineProgress progress = progressRepository.findByRoutineIdAndProgressDate(routineId, date)
                .orElseGet(() -> RoutineProgress.builder()
                        .routineId(routineId)
                        .userId(userId)
                        .progressDate(date)
                        .amount(0)
                        .status(ProgressStatus.SKIPPED)
                        .build());

        progress.updateProgress(0, ProgressStatus.SKIPPED);
        RoutineProgress saved = progressRepository.save(progress);
        return progressMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProgress(UUID userId, UUID routineId, LocalDate date) {
        Routine routine = getRoutineEntity(userId, routineId);
        RoutineProgress progress = progressRepository.findByRoutineIdAndProgressDate(routineId, date)
                .orElseThrow(() -> new NotFoundException(RoutineMessages.ROUTINE_PROGRESS_NOT_FOUND));
        if (!progress.getUserId().equals(userId)) {
            throw new NotFoundException(RoutineMessages.ROUTINE_PROGRESS_NOT_FOUND);
        }
        if (routine.getId().equals(progress.getRoutineId())) {
            progressRepository.delete(progress);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoutineProgressResponse> listProgress(UUID userId, UUID routineId, LocalDate from, LocalDate to) {
        Routine routine = getRoutineEntity(userId, routineId);
        LocalDate effectiveFrom;
        LocalDate effectiveTo;
        if (from == null && to == null) {
            UserPreferencesResponse preferences = userPreferencesService.getPreferences(userId);
            LocalDate[] weekRange = resolveWeekRange(preferences, null);
            effectiveFrom = resolveFromDate(weekRange[0], routine.getStartDate());
            effectiveTo = resolveToDate(weekRange[1], routine.getEndDate());
        } else {
            effectiveFrom = resolveFromDate(from, routine.getStartDate());
            effectiveTo = resolveToDate(to, routine.getEndDate());
        }

        return progressRepository.findByRoutineIdAndProgressDateBetweenOrderByProgressDate(
                        routineId,
                        effectiveFrom,
                        effectiveTo
                ).stream()
                .filter(progress -> progress.getUserId().equals(userId))
                .map(progressMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoutineSummaryResponse getSummary(UUID userId, UUID routineId, LocalDate from, LocalDate to) {
        Routine routine = getRoutineEntity(userId, routineId);
        LocalDate effectiveFrom;
        LocalDate effectiveTo;
        if (from == null && to == null) {
            UserPreferencesResponse preferences = userPreferencesService.getPreferences(userId);
            LocalDate[] weekRange = resolveWeekRange(preferences, null);
            effectiveFrom = resolveFromDate(weekRange[0], routine.getStartDate());
            effectiveTo = resolveToDate(weekRange[1], routine.getEndDate());
        } else {
            effectiveFrom = resolveFromDate(from, routine.getStartDate());
            effectiveTo = resolveToDate(to, routine.getEndDate());
        }

        List<RoutineDayOfWeek> scheduleDays = getDaysOfWeek(routineId);
        Set<LocalDate> targetDates = buildTargetDates(effectiveFrom, effectiveTo, scheduleDays);
        int totalTargetDays = targetDates.size();

        List<RoutineProgress> progressList = progressRepository
                .findByRoutineIdAndProgressDateBetweenOrderByProgressDate(
                        routineId,
                        effectiveFrom,
                        effectiveTo
                );

        Set<LocalDate> completedDates = new HashSet<>();
        for (RoutineProgress progress : progressList) {
            if (progress.getUserId().equals(userId)
                    && progress.getStatus() == ProgressStatus.COMPLETED
                    && targetDates.contains(progress.getProgressDate())) {
                completedDates.add(progress.getProgressDate());
            }
        }

        int completedDays = completedDates.size();
        double completionRate = totalTargetDays == 0
                ? 0.0
                : (double) completedDays / totalTargetDays;

        List<LocalDate> sortedTargets = new ArrayList<>(targetDates);
        sortedTargets.sort(LocalDate::compareTo);
        int longestStreak = calculateLongestStreak(sortedTargets, completedDates);
        int currentStreak = calculateCurrentStreak(sortedTargets, completedDates);

        return new RoutineSummaryResponse(
                completionRate,
                currentStreak,
                longestStreak,
                completedDays,
                totalTargetDays
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RoutineOverviewResponse getOverview(UUID userId, LocalDate date) {
        LocalDate targetDate = resolveUserDate(userId, date);
        RoutineDayOfWeek dayOfWeek = RoutineDayOfWeek.from(targetDate.getDayOfWeek());

        int todayRequiredCount = routineRepository.findTodayRoutines(userId, targetDate, dayOfWeek).size();
        int todayCompletedCount = (int) progressRepository.countByUserIdAndProgressDateAndStatus(
                userId,
                targetDate,
                ProgressStatus.COMPLETED
        );
        int activeRoutineCount = routineRepository.findByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId).size();

        return new RoutineOverviewResponse(todayRequiredCount, todayCompletedCount, activeRoutineCount);
    }

    @Override
    @Transactional(readOnly = true)
    public RoutineRemindersResponse getReminders(UUID userId, UUID routineId) {
        Routine routine = getRoutineEntity(userId, routineId);
        List<LocalTime> times = reminderRepository.findByRoutineIdOrderByReminderTime(routine.getId()).stream()
                .map(RoutineReminder::getReminderTime)
                .toList();
        return new RoutineRemindersResponse(routine.getId(), times);
    }

    @Override
    @Transactional
    public RoutineRemindersResponse updateReminders(
            UUID userId,
            UUID routineId,
            UpdateRoutineRemindersRequest request
    ) {
        Routine routine = getRoutineEntity(userId, routineId);
        List<LocalTime> times = normalizeReminderTimes(request.times());
        reminderRepository.deleteByRoutineId(routine.getId());
        if (!times.isEmpty()) {
            List<RoutineReminder> reminders = times.stream()
                    .map(time -> new RoutineReminder(routine.getId(), time))
                    .toList();
            reminderRepository.saveAll(reminders);
        }
        return new RoutineRemindersResponse(routine.getId(), times);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveRoutines(UUID userId) {
        return routineRepository.countByUserIdAndIsActiveTrueAndDeletedAtIsNull(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCompletedProgressDays(UUID userId, LocalDate from, LocalDate to) {
        return progressRepository.countByUserIdAndProgressDateBetweenAndStatus(
                userId,
                from,
                to,
                ProgressStatus.COMPLETED
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Long> countCompletedProgressByDate(UUID userId, LocalDate from, LocalDate to) {
        return toDateCountMap(
                progressRepository.countCompletedByDate(userId, from, to, ProgressStatus.COMPLETED)
        );
    }

    private Routine getRoutineEntity(UUID userId, UUID routineId) {
        return routineRepository.findByIdAndUserIdAndDeletedAtIsNull(routineId, userId)
                .orElseThrow(() -> new NotFoundException(RoutineMessages.ROUTINE_NOT_FOUND));
    }

    private void saveSchedule(UUID routineId, List<RoutineDayOfWeek> daysOfWeek) {
        List<RoutineSchedule> schedules = daysOfWeek.stream()
                .map(day -> new RoutineSchedule(routineId, day))
                .toList();
        scheduleRepository.saveAll(schedules);
    }

    private List<RoutineDayOfWeek> getDaysOfWeek(UUID routineId) {
        return scheduleRepository.findByRoutineId(routineId).stream()
                .map(RoutineSchedule::getDayOfWeek)
                .toList();
    }

    private RoutineResponse toRoutineResponse(Routine routine, List<RoutineDayOfWeek> daysOfWeek) {
        RoutineScheduleResponse scheduleResponse = new RoutineScheduleResponse(daysOfWeek);
        return routineMapper.toResponse(routine, scheduleResponse);
    }

    private Map<UUID, List<RoutineDayOfWeek>> mapSchedules(List<RoutineSchedule> schedules) {
        Map<UUID, List<RoutineDayOfWeek>> map = new HashMap<>();
        for (RoutineSchedule schedule : schedules) {
            map.computeIfAbsent(schedule.getRoutineId(), key -> new ArrayList<>())
                    .add(schedule.getDayOfWeek());
        }
        return map;
    }

    private List<LocalTime> normalizeReminderTimes(List<LocalTime> times) {
        if (times == null || times.isEmpty()) {
            return List.of();
        }
        Set<LocalTime> unique = new HashSet<>(times);
        List<LocalTime> normalized = new ArrayList<>(unique);
        normalized.sort(LocalTime::compareTo);
        return normalized;
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

    private List<RoutineDayOfWeek> normalizeDays(List<RoutineDayOfWeek> daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            throw new BadRequestException(RoutineMessages.ROUTINE_SCHEDULE_REQUIRED);
        }
        return new ArrayList<>(EnumSet.copyOf(daysOfWeek));
    }

    private Boolean parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        if ("active".equalsIgnoreCase(status)) {
            return true;
        }
        if ("inactive".equalsIgnoreCase(status)) {
            return false;
        }
        throw new BadRequestException("Invalid status filter");
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.trim();
    }

    private void assertRoutineActive(Routine routine) {
        if (!Boolean.TRUE.equals(routine.getIsActive())) {
            throw new BadRequestException(RoutineMessages.ROUTINE_INACTIVE);
        }
    }

    private void validateProgressDate(Routine routine, LocalDate date) {
        if (date == null) {
            throw new BadRequestException(RoutineMessages.ROUTINE_PROGRESS_DATE_INVALID);
        }
        if (routine.getStartDate() != null && date.isBefore(routine.getStartDate())) {
            throw new BadRequestException(RoutineMessages.ROUTINE_PROGRESS_DATE_INVALID);
        }
        if (routine.getEndDate() != null && date.isAfter(routine.getEndDate())) {
            throw new BadRequestException(RoutineMessages.ROUTINE_PROGRESS_DATE_INVALID);
        }
    }

    private void validateSchedule(UUID routineId, LocalDate date) {
        RoutineDayOfWeek dayOfWeek = RoutineDayOfWeek.from(date.getDayOfWeek());
        List<RoutineDayOfWeek> daysOfWeek = getDaysOfWeek(routineId);
        if (!daysOfWeek.contains(dayOfWeek)) {
            throw new BadRequestException(RoutineMessages.ROUTINE_PROGRESS_DAY_INVALID);
        }
    }

    private void validateProgressAmount(Routine routine, Integer amount) {
        if (amount == null || amount < 0) {
            throw new BadRequestException(RoutineMessages.ROUTINE_PROGRESS_AMOUNT_INVALID);
        }
        if (routine.getType() == RoutineType.CHECK) {
            if (amount != 1) {
                throw new BadRequestException(RoutineMessages.ROUTINE_PROGRESS_AMOUNT_INVALID);
            }
        } else if (routine.getType() == RoutineType.NUMERIC) {
            if (routine.getTargetValue() != null && amount > routine.getTargetValue()) {
                throw new BadRequestException(RoutineMessages.ROUTINE_PROGRESS_AMOUNT_INVALID);
            }
        }
    }

    private ProgressStatus resolveStatus(Routine routine, Integer amount) {
        if (routine.getType() == RoutineType.CHECK) {
            return ProgressStatus.COMPLETED;
        }
        if (routine.getTargetValue() != null && amount >= routine.getTargetValue()) {
            return ProgressStatus.COMPLETED;
        }
        return ProgressStatus.IN_PROGRESS;
    }

    private LocalDate resolveUserDate(UUID userId, LocalDate date) {
        UserPreferencesResponse preferences = userPreferencesService.getPreferences(userId);
        return resolveUserDate(preferences, date);
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
        } catch (java.time.DateTimeException e) {
            return LocalDate.now();
        }
    }

    private LocalDate[] resolveWeekRange(UserPreferencesResponse preferences, LocalDate date) {
        LocalDate baseDate = resolveUserDate(preferences, date);
        WeekStartDay startDay = preferences.weekStartDay() != null
                ? preferences.weekStartDay()
                : WeekStartDay.MONDAY;
        int dayValue = baseDate.getDayOfWeek().getValue();
        int offset = startDay == WeekStartDay.SUNDAY ? dayValue % 7 : dayValue - 1;
        LocalDate start = baseDate.minusDays(offset);
        return new LocalDate[]{start, start.plusDays(6)};
    }

    private LocalDate resolveFromDate(LocalDate from, LocalDate startDate) {
        if (from == null && startDate == null) {
            throw new BadRequestException("from date is required");
        }
        if (from == null) {
            return startDate;
        }
        if (startDate != null && from.isBefore(startDate)) {
            return startDate;
        }
        return from;
    }

    private LocalDate resolveToDate(LocalDate to, LocalDate endDate) {
        if (to == null && endDate == null) {
            throw new BadRequestException("to date is required");
        }
        if (to == null) {
            return endDate;
        }
        if (endDate != null && to.isAfter(endDate)) {
            return endDate;
        }
        return to;
    }

    private Set<LocalDate> buildTargetDates(
            LocalDate from,
            LocalDate to,
            List<RoutineDayOfWeek> scheduleDays
    ) {
        if (from == null || to == null || scheduleDays.isEmpty() || from.isAfter(to)) {
            return Set.of();
        }
        Set<RoutineDayOfWeek> days = EnumSet.copyOf(scheduleDays);
        Set<LocalDate> targetDates = new HashSet<>();
        LocalDate cursor = from;
        while (!cursor.isAfter(to)) {
            RoutineDayOfWeek day = RoutineDayOfWeek.from(cursor.getDayOfWeek());
            if (days.contains(day)) {
                targetDates.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }
        return targetDates;
    }

    private int calculateLongestStreak(List<LocalDate> targetDates, Set<LocalDate> completedDates) {
        int longest = 0;
        int current = 0;
        for (LocalDate date : targetDates) {
            if (completedDates.contains(date)) {
                current += 1;
                if (current > longest) {
                    longest = current;
                }
            } else {
                current = 0;
            }
        }
        return longest;
    }

    private int calculateCurrentStreak(List<LocalDate> targetDates, Set<LocalDate> completedDates) {
        int streak = 0;
        for (int i = targetDates.size() - 1; i >= 0; i--) {
            LocalDate date = targetDates.get(i);
            if (completedDates.contains(date)) {
                streak += 1;
            } else {
                break;
            }
        }
        return streak;
    }
}

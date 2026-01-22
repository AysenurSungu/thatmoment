package com.thatmoment.modules.routine.service;

import com.thatmoment.common.dto.MessageResponse;
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
import com.thatmoment.modules.routine.dto.response.RoutineSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RoutineService {

    RoutineResponse createRoutine(UUID userId, CreateRoutineRequest request);

    RoutineResponse getRoutine(UUID userId, UUID routineId);

    Page<RoutineResponse> listRoutines(UUID userId, String status, String query, Pageable pageable);

    RoutineResponse updateRoutine(UUID userId, UUID routineId, UpdateRoutineRequest request);

    void deleteRoutine(UUID userId, UUID routineId);

    MessageResponse activateRoutine(UUID userId, UUID routineId);

    MessageResponse deactivateRoutine(UUID userId, UUID routineId);

    List<RoutineResponse> listTodayRoutines(UUID userId);

    List<RoutineResponse> listActiveRoutines(UUID userId);

    RoutineProgressResponse addProgress(UUID userId, UUID routineId, CreateRoutineProgressRequest request);

    RoutineProgressResponse updateProgress(
            UUID userId,
            UUID routineId,
            LocalDate date,
            UpdateRoutineProgressRequest request
    );

    RoutineProgressResponse skipRoutine(UUID userId, UUID routineId, SkipRoutineRequest request);

    void deleteProgress(UUID userId, UUID routineId, LocalDate date);

    List<RoutineProgressResponse> listProgress(UUID userId, UUID routineId, LocalDate from, LocalDate to);

    RoutineSummaryResponse getSummary(UUID userId, UUID routineId, LocalDate from, LocalDate to);

    RoutineOverviewResponse getOverview(UUID userId, LocalDate date);

    RoutineRemindersResponse getReminders(UUID userId, UUID routineId);

    RoutineRemindersResponse updateReminders(UUID userId, UUID routineId, UpdateRoutineRemindersRequest request);

    long countActiveRoutines(UUID userId);

    long countCompletedProgressDays(UUID userId, LocalDate from, LocalDate to);

    Map<LocalDate, Long> countCompletedProgressByDate(UUID userId, LocalDate from, LocalDate to);
}

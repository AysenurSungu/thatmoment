package com.thatmoment.modules.routine.api;

import com.thatmoment.common.constants.ApiDescriptions;
import com.thatmoment.common.dto.MessageResponse;
import com.thatmoment.modules.auth.security.UserPrincipal;
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
import com.thatmoment.modules.routine.service.RoutineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/routines")
@Tag(name = ApiDescriptions.TAG_ROUTINE, description = ApiDescriptions.TAG_ROUTINE_DESC)
@PreAuthorize("isAuthenticated()")
public class RoutineController {

    private final RoutineService routineService;

    public RoutineController(RoutineService routineService) {
        this.routineService = routineService;
    }

    @PostMapping
    @Operation(summary = ApiDescriptions.ROUTINE_CREATE_SUMMARY)
    @ResponseStatus(HttpStatus.CREATED)
    public RoutineResponse createRoutine(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateRoutineRequest request
    ) {
        return routineService.createRoutine(principal.getUserId(), request);
    }

    @GetMapping
    @Operation(summary = ApiDescriptions.ROUTINE_LIST_SUMMARY)
    public Page<RoutineResponse> listRoutines(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20)
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return routineService.listRoutines(principal.getUserId(), status, query, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = ApiDescriptions.ROUTINE_GET_SUMMARY)
    public RoutineResponse getRoutine(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return routineService.getRoutine(principal.getUserId(), id);
    }

    @PutMapping("/{id}")
    @Operation(summary = ApiDescriptions.ROUTINE_UPDATE_SUMMARY)
    public RoutineResponse updateRoutine(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoutineRequest request
    ) {
        return routineService.updateRoutine(principal.getUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = ApiDescriptions.ROUTINE_DELETE_SUMMARY)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoutine(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        routineService.deleteRoutine(principal.getUserId(), id);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = ApiDescriptions.ROUTINE_ACTIVATE_SUMMARY)
    public MessageResponse activateRoutine(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return routineService.activateRoutine(principal.getUserId(), id);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = ApiDescriptions.ROUTINE_DEACTIVATE_SUMMARY)
    public MessageResponse deactivateRoutine(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return routineService.deactivateRoutine(principal.getUserId(), id);
    }

    @GetMapping("/today")
    @Operation(summary = ApiDescriptions.ROUTINE_TODAY_SUMMARY)
    public List<RoutineResponse> listTodayRoutines(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return routineService.listTodayRoutines(principal.getUserId());
    }

    @GetMapping("/active")
    @Operation(summary = ApiDescriptions.ROUTINE_ACTIVE_SUMMARY)
    public List<RoutineResponse> listActiveRoutines(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return routineService.listActiveRoutines(principal.getUserId());
    }

    @PostMapping("/{id}/progress")
    @Operation(summary = ApiDescriptions.ROUTINE_PROGRESS_CREATE_SUMMARY)
    public RoutineProgressResponse addProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody CreateRoutineProgressRequest request
    ) {
        return routineService.addProgress(principal.getUserId(), id, request);
    }

    @PutMapping("/{id}/progress/{date}")
    @Operation(summary = ApiDescriptions.ROUTINE_PROGRESS_UPDATE_SUMMARY)
    public RoutineProgressResponse updateProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @PathVariable LocalDate date,
            @Valid @RequestBody UpdateRoutineProgressRequest request
    ) {
        return routineService.updateProgress(principal.getUserId(), id, date, request);
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = ApiDescriptions.ROUTINE_PROGRESS_LIST_SUMMARY)
    public List<RoutineProgressResponse> listProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return routineService.listProgress(principal.getUserId(), id, from, to);
    }

    @PostMapping("/{id}/skip")
    @Operation(summary = ApiDescriptions.ROUTINE_SKIP_SUMMARY)
    public RoutineProgressResponse skipRoutine(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody SkipRoutineRequest request
    ) {
        return routineService.skipRoutine(principal.getUserId(), id, request);
    }

    @DeleteMapping("/{id}/progress/{date}")
    @Operation(summary = ApiDescriptions.ROUTINE_PROGRESS_DELETE_SUMMARY)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @PathVariable LocalDate date
    ) {
        routineService.deleteProgress(principal.getUserId(), id, date);
    }

    @GetMapping("/{id}/summary")
    @Operation(summary = ApiDescriptions.ROUTINE_SUMMARY_SUMMARY)
    public RoutineSummaryResponse getSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to
    ) {
        return routineService.getSummary(principal.getUserId(), id, from, to);
    }

    @GetMapping("/{id}/reminders")
    @Operation(summary = ApiDescriptions.ROUTINE_REMINDERS_GET_SUMMARY)
    public RoutineRemindersResponse getReminders(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return routineService.getReminders(principal.getUserId(), id);
    }

    @PutMapping("/{id}/reminders")
    @Operation(summary = ApiDescriptions.ROUTINE_REMINDERS_UPDATE_SUMMARY)
    public RoutineRemindersResponse updateReminders(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoutineRemindersRequest request
    ) {
        return routineService.updateReminders(principal.getUserId(), id, request);
    }

    @GetMapping("/overview")
    @Operation(summary = ApiDescriptions.ROUTINE_OVERVIEW_SUMMARY)
    public RoutineOverviewResponse getOverview(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) LocalDate date
    ) {
        return routineService.getOverview(principal.getUserId(), date);
    }
}

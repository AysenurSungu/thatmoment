package com.thatmoment.modules.journal.api;

import com.thatmoment.modules.auth.security.UserPrincipal;
import com.thatmoment.common.constants.ApiDescriptions;
import com.thatmoment.modules.journal.dto.request.CreateJournalEntryRequest;
import com.thatmoment.modules.journal.dto.request.UpdateJournalEntryRequest;
import com.thatmoment.modules.journal.dto.response.JournalEntryResponse;
import com.thatmoment.modules.journal.service.JournalEntryService;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journal/entries")
@Tag(name = ApiDescriptions.TAG_JOURNAL, description = ApiDescriptions.TAG_JOURNAL_DESC)
@PreAuthorize("isAuthenticated()")
public class JournalEntryController {

    private final JournalEntryService entryService;

    public JournalEntryController(JournalEntryService entryService) {
        this.entryService = entryService;
    }

    @PostMapping
    @Operation(summary = ApiDescriptions.JOURNAL_ENTRY_CREATE_SUMMARY)
    @ResponseStatus(HttpStatus.CREATED)
    public JournalEntryResponse createEntry(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateJournalEntryRequest request
    ) {
        return entryService.createEntry(principal.getUserId(), request);
    }

    @GetMapping("/{id}")
    @Operation(summary = ApiDescriptions.JOURNAL_ENTRY_GET_SUMMARY)
    public JournalEntryResponse getEntry(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return entryService.getEntry(principal.getUserId(), id);
    }

    @GetMapping
    @Operation(summary = ApiDescriptions.JOURNAL_ENTRY_LIST_SUMMARY)
    public Page<JournalEntryResponse> listEntries(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20)
            @SortDefault(sort = "entryDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) LocalDate date
    ) {
        return entryService.listEntries(principal.getUserId(), date, pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = ApiDescriptions.JOURNAL_ENTRY_UPDATE_SUMMARY)
    public JournalEntryResponse updateEntry(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJournalEntryRequest request
    ) {
        return entryService.updateEntry(principal.getUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = ApiDescriptions.JOURNAL_ENTRY_DELETE_SUMMARY)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        entryService.deleteEntry(principal.getUserId(), id);
    }
}

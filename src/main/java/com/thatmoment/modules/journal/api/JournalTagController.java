package com.thatmoment.modules.journal.api;

import com.thatmoment.modules.auth.security.UserPrincipal;
import com.thatmoment.common.constants.ApiDescriptions;
import com.thatmoment.modules.journal.dto.request.CreateJournalTagRequest;
import com.thatmoment.modules.journal.dto.request.UpdateJournalTagRequest;
import com.thatmoment.modules.journal.dto.response.JournalTagResponse;
import com.thatmoment.modules.journal.service.JournalTagService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journal/tags")
@Tag(name = ApiDescriptions.TAG_JOURNAL, description = ApiDescriptions.TAG_JOURNAL_DESC)
@PreAuthorize("isAuthenticated()")
public class JournalTagController {

    private final JournalTagService tagService;

    public JournalTagController(JournalTagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    @Operation(summary = ApiDescriptions.JOURNAL_TAG_CREATE_SUMMARY)
    @ResponseStatus(HttpStatus.CREATED)
    public JournalTagResponse createTag(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateJournalTagRequest request
    ) {
        return tagService.createTag(principal.getUserId(), request);
    }

    @GetMapping("/{id}")
    @Operation(summary = ApiDescriptions.JOURNAL_TAG_GET_SUMMARY)
    public JournalTagResponse getTag(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        return tagService.getTag(principal.getUserId(), id);
    }

    @GetMapping
    @Operation(summary = ApiDescriptions.JOURNAL_TAG_LIST_SUMMARY)
    public Page<JournalTagResponse> listTags(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20)
            @SortDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return tagService.listTags(principal.getUserId(), pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = ApiDescriptions.JOURNAL_TAG_UPDATE_SUMMARY)
    public JournalTagResponse updateTag(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJournalTagRequest request
    ) {
        return tagService.updateTag(principal.getUserId(), id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = ApiDescriptions.JOURNAL_TAG_DELETE_SUMMARY)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTag(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id
    ) {
        tagService.deleteTag(principal.getUserId(), id);
    }
}

package com.thatmoment.modules.journal.service;

import com.thatmoment.modules.journal.dto.request.CreateJournalEntryRequest;
import com.thatmoment.modules.journal.dto.request.UpdateJournalEntryRequest;
import com.thatmoment.modules.journal.dto.response.JournalEntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface JournalEntryService {

    JournalEntryResponse createEntry(UUID userId, CreateJournalEntryRequest request);

    JournalEntryResponse getEntry(UUID userId, UUID entryId);

    Page<JournalEntryResponse> listEntries(UUID userId, LocalDate date, Pageable pageable);

    JournalEntryResponse updateEntry(UUID userId, UUID entryId, UpdateJournalEntryRequest request);

    void deleteEntry(UUID userId, UUID entryId);
}

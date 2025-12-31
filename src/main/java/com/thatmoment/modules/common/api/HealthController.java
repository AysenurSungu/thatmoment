package com.thatmoment.modules.common.api;

import com.thatmoment.modules.common.constants.ApiDescriptions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = ApiDescriptions.TAG_HEALTH, description = ApiDescriptions.TAG_HEALTH_DESC)
public class HealthController {

    @GetMapping
    @Operation(
            summary = ApiDescriptions.HEALTH_SUMMARY,
            description = ApiDescriptions.HEALTH_DESCRIPTION
    )
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", "thatmoment-api");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}

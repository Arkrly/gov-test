package com.ourvoiceourrights.controller;

import com.ourvoiceourrights.repository.MgnregaPerformanceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Operational insights")
public class HealthController {

    private final MgnregaPerformanceRepository performanceRepository;

    @Operation(summary = "Report ingestion freshness")
    @GetMapping("/ingestion")
    public ResponseEntity<Map<String, Object>> ingestion() {
        Instant threshold = Instant.now().minus(Duration.ofHours(24));
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "UP");
        performanceRepository.findMostRecentUpdateSince(threshold)
                .ifPresentOrElse(
                        latest -> {
                            payload.put("latestUpdate", latest);
                            payload.put("stale", false);
                        },
                        () -> payload.put("stale", true));
        return ResponseEntity.ok(payload);
    }
}

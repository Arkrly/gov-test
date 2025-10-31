package com.ourvoiceourrights.controller;

import com.ourvoiceourrights.dto.PerformanceDto;
import com.ourvoiceourrights.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Validated
@Tag(name = "Performance", description = "MGNREGA performance metrics")
public class PerformanceController {

    private final PerformanceService performanceService;

    @Operation(summary = "Get latest performance for a district")
    @GetMapping("/{district}")
    public ResponseEntity<PerformanceDto> latest(
            @PathVariable("district") @NotBlank String district,
            @RequestParam(value = "fin_year", required = false) String finYear) {
        return ResponseEntity.ok(performanceService.getLatest(district, finYear));
    }

    @Operation(summary = "List historical performance for a district")
    @GetMapping("/{district}/history")
    public ResponseEntity<List<PerformanceDto>> history(
            @PathVariable("district") @NotBlank String district,
            @RequestParam(value = "fin_year", required = false) String finYear,
            @RequestParam(value = "limit", defaultValue = "50") @Min(1) @Max(500) int limit,
            @RequestParam(value = "offset", defaultValue = "0") @Min(0) int offset,
            @Parameter(description = "Filter records updated after this instant")
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "Filter records updated before this instant")
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseEntity.ok(performanceService.getHistory(district, finYear, limit, offset, from, to));
    }
}

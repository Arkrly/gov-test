package com.ourvoiceourrights.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;

@Schema(name = "Performance")
public record PerformanceDto(
        @Schema(description = "District name") String district,
        @Schema(description = "State name") String state,
        @Schema(description = "Financial year identifier, e.g. 2023-2024") String finYear,
        @Schema(description = "Month number when available") Integer month,
        @Schema(description = "Total person-days recorded") Long totalPersondays,
        @Schema(description = "Total households worked") Long totalHouseholds,
        @Schema(description = "Total expenditure in INR") BigDecimal expenditure,
        @Schema(description = "Last update timestamp") Instant updatedAt
) {
}

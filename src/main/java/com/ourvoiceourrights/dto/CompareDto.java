package com.ourvoiceourrights.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "Comparison")
public record CompareDto(
        @Schema(description = "Latest metrics for the first district") PerformanceDto district1,
        @Schema(description = "Latest metrics for the second district") PerformanceDto district2,
        @Schema(description = "Delta in person-days (district1 - district2)") Long totalPersondaysDelta,
        @Schema(description = "Delta in households (district1 - district2)") Long totalHouseholdsDelta,
        @Schema(description = "Delta in expenditure (district1 - district2)") BigDecimal expenditureDelta
) {
}

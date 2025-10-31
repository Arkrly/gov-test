package com.ourvoiceourrights.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "District")
public record DistrictDto(
        @Schema(description = "Internal identifier") Long id,
        @Schema(description = "District name") String name,
        @Schema(description = "District code from MGNREGA dataset") String code,
        @Schema(description = "Parent state identifier") Long stateId
) {
}

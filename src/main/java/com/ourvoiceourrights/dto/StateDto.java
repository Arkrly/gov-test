package com.ourvoiceourrights.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "State")
public record StateDto(
        @Schema(description = "Internal identifier") Long id,
        @Schema(description = "State name") String name,
        @Schema(description = "State code derived from MGNREGA dataset") String code
) {
}

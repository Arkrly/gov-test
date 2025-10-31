package com.ourvoiceourrights.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GeoReverse")
public record GeoReverseDto(
        @Schema(description = "Formatted address when available") String address,
        @Schema(description = "Resolved state name") String state,
        @Schema(description = "Resolved district name") String district,
        @Schema(description = "Detection strategy used") String method,
        @Schema(description = "Estimated accuracy radius in kilometers") Double accuracyRadiusKm
) {
}

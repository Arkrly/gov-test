package com.ourvoiceourrights.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GeoDetection")
public record GeoDetectDto(
        @Schema(description = "Detected state name") String state,
        @Schema(description = "Detected district name") String district,
        @Schema(description = "Detection strategy used") String method,
        @Schema(description = "Estimated accuracy radius in kilometers") Double accuracyRadiusKm
) {
}

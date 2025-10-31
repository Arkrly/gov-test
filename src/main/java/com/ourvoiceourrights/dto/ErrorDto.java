package com.ourvoiceourrights.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(name = "Error")
public record ErrorDto(
        @Schema(description = "Event timestamp in UTC") Instant timestamp,
        @Schema(description = "HTTP status code") int status,
        @Schema(description = "Status phrase") String error,
        @Schema(description = "Human readable message") String message,
        @Schema(description = "Request path") String path,
        @Schema(description = "Trace identifier for debugging") String traceId
) {
}

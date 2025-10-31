package com.ourvoiceourrights.controller;

import com.ourvoiceourrights.dto.GeoDetectDto;
import com.ourvoiceourrights.dto.GeoReverseDto;
import com.ourvoiceourrights.service.GeoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
@Tag(name = "Geo", description = "Geo-detection endpoints")
public class GeoController {

    private final GeoService geoService;

    @Operation(summary = "Detect district from coordinates or IP")
    @GetMapping("/detect")
    public ResponseEntity<GeoDetectDto> detect(@RequestParam(value = "lat", required = false) Double latitude,
                                               @RequestParam(value = "lon", required = false) Double longitude,
                                               HttpServletRequest request) {
        String clientIp = extractClientIp(request);
        Optional<GeoDetectDto> detected = geoService.detect(latitude, longitude, clientIp);
        return detected.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Reverse geocode coordinates into district and state")
    @GetMapping("/reverse")
    public ResponseEntity<GeoReverseDto> reverse(@RequestParam("lat") Double latitude,
                                                 @RequestParam("lon") Double longitude) {
        return geoService.reverse(latitude, longitude)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

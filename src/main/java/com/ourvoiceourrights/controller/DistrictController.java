package com.ourvoiceourrights.controller;

import com.ourvoiceourrights.dto.DistrictDto;
import com.ourvoiceourrights.service.DistrictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/districts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Districts", description = "District directory endpoints")
public class DistrictController {

    private final DistrictService districtService;

    @Operation(summary = "List districts within a state")
    @GetMapping
    public ResponseEntity<List<DistrictDto>> listDistricts(
            @Parameter(description = "State code or name", required = true)
            @RequestParam("state") @NotBlank String state) {
        return ResponseEntity.ok(districtService.listDistricts(state));
    }
}

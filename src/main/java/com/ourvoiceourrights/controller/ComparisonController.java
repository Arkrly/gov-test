package com.ourvoiceourrights.controller;

import com.ourvoiceourrights.dto.CompareDto;
import com.ourvoiceourrights.service.ComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compare")
@RequiredArgsConstructor
@Validated
@Tag(name = "Comparison", description = "Compare district performance")
public class ComparisonController {

    private final ComparisonService comparisonService;

    @Operation(summary = "Compare two districts")
    @GetMapping
    public ResponseEntity<CompareDto> compare(
            @RequestParam("district1") @NotBlank String districtOne,
            @RequestParam("district2") @NotBlank String districtTwo) {
        return ResponseEntity.ok(comparisonService.compare(districtOne, districtTwo));
    }
}

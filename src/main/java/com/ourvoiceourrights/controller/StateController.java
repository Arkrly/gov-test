package com.ourvoiceourrights.controller;

import com.ourvoiceourrights.dto.StateDto;
import com.ourvoiceourrights.service.StateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/states")
@RequiredArgsConstructor
@Tag(name = "States", description = "List states supported by the platform")
public class StateController {

    private final StateService stateService;

    @Operation(summary = "List states")
    @GetMapping
    public ResponseEntity<List<StateDto>> listStates() {
        return ResponseEntity.ok(stateService.listStates());
    }
}

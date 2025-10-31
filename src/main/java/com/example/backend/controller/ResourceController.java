package com.example.backend.controller;

import com.example.backend.config.DataGovProperties;
import com.example.backend.model.ResourceResponse;
import com.example.backend.model.ResourceResponse.ResourceQuery;
import com.example.backend.model.ResourceResponse.ResourceRecord;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/resource")
@Validated
public class ResourceController {

    private static final String RESOURCE_ID = "ee03643a-ee4c-48c2-ac30-9f2ff26ab722";
    private static final List<String> SUPPORTED_FORMATS = List.of("json", "xml", "csv");

    private final DataGovProperties properties;

    public ResourceController(DataGovProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/{resourceId}")
    public ResponseEntity<ResourceResponse> proxyResource(
            @PathVariable String resourceId,
            @RequestParam(name = "api-key") @NotBlank String apiKey,
            @RequestParam(name = "format", defaultValue = "json") String format,
            @RequestParam(name = "offset", required = false) @Min(0) Integer offset,
            @RequestParam(name = "limit", required = false) @Min(1) Integer limit,
            @RequestParam(name = "filters[state_name]", required = false) String stateName,
            @RequestParam(name = "filters[fin_year]", required = false) String finYear
    ) {
        if (!RESOURCE_ID.equals(resourceId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown resource: " + resourceId);
        }

        String trimmedFormat = format == null ? "json" : format.trim().toLowerCase(Locale.US);
        if (!SUPPORTED_FORMATS.contains(trimmedFormat)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported format. Allowed values: " + String.join(", ", SUPPORTED_FORMATS));
        }

        String expectedApiKey = properties.getApiKey();
        if (expectedApiKey != null && !expectedApiKey.isBlank() && !expectedApiKey.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid api-key");
        }

        Map<String, String> filters = new LinkedHashMap<>();
        if (stateName != null && !stateName.isBlank()) {
            filters.put("state_name", stateName);
        }
        if (finYear != null && !finYear.isBlank()) {
            filters.put("fin_year", finYear);
        }

        ResourceQuery query = new ResourceQuery(apiKey, trimmedFormat, offset, limit,
                filters.isEmpty() ? null : filters);

        ResourceRecord record = new ResourceRecord(
                "KARNATAKA",
                "BENGALURU RURAL",
                finYear != null ? finYear : "2023-2024",
                "APRIL",
                12345,
                567,
                98000
        );

        ResourceResponse response = ResourceResponse.of(RESOURCE_ID, query, List.of(record));

        return ResponseEntity.ok(response);
    }
}

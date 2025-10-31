package com.example.backend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceResponse(
        String resourceId,
        String status,
        ResourceQuery query,
        List<ResourceRecord> records
) {
    public static ResourceResponse of(String resourceId,
                                      ResourceQuery query,
                                      List<ResourceRecord> records) {
        return new ResourceResponse(resourceId, "ok", query, records);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ResourceQuery(
            @JsonProperty("api-key") String apiKey,
            String format,
            Integer offset,
            Integer limit,
            Map<String, String> filters
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ResourceRecord(
            @JsonProperty("state_name") String stateName,
            @JsonProperty("district_name") String districtName,
            @JsonProperty("fin_year") String finYear,
            String month,
            @JsonProperty("total_persondays") int totalPersondays,
            @JsonProperty("total_households") int totalHouseholds,
            int expenditure
    ) {
    }
}

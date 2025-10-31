package com.ourvoiceourrights.client;

import com.ourvoiceourrights.config.AppProperties;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DataGovClient {

    private static final String RETRY_NAME = "datagov";
    private static final String CIRCUIT_BREAKER_NAME = "datagov";

    private final WebClient webClient;
    private final AppProperties appProperties;
    private final String apiKey;

    public DataGovClient(WebClient.Builder builder,
                         AppProperties appProperties,
                         @Value("${DATA_GOV_API_KEY:}") String apiKey) {
        this.webClient = builder
                .baseUrl(appProperties.getDataGovBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .build();
        this.appProperties = appProperties;
        this.apiKey = apiKey;
    }

    @PostConstruct
    void checkConfiguration() {
        if (StringUtils.isBlank(apiKey)) {
            log.warn("DATA_GOV_API_KEY is not configured; ingestion will fail to authenticate against DataGov API");
        }
    }

    @Retry(name = RETRY_NAME)
    @CircuitBreaker(name = CIRCUIT_BREAKER_NAME, fallbackMethod = "handleFailure")
    public DataGovPage fetchMetrics(String state, String finYear, int limit, int offset) {
        long start = System.nanoTime();
        return webClient.get()
                .uri(uriBuilder -> buildUri(uriBuilder, state, finYear, limit, offset))
                .retrieve()
                .bodyToMono(DataGovResponse.class)
                .timeout(Duration.ofSeconds(15))
                .map(response -> mapResponse(state, finYear, limit, offset, response))
                .doOnError(error -> log.error("Failed to fetch DataGov metrics state={} finYear={} offset={}", state, finYear, offset, error))
                .doOnSuccess(page -> log.debug("Fetched {} DataGov records in {} ms", page.records().size(), (System.nanoTime() - start) / 1_000_000))
                .blockOptional()
                .orElseGet(DataGovPage::empty);
    }

    private DataGovPage handleFailure(String state, String finYear, int limit, int offset, Throwable throwable) {
        log.warn("DataGov API returning fallback state={} finYear={} offset={}: {}", state, finYear, offset, throwable.getMessage());
        return DataGovPage.empty();
    }

    private java.net.URI buildUri(UriBuilder builder, String state, String finYear, int limit, int offset) {
        builder.path("/{resourceId}")
                .queryParam("api-key", apiKey)
                .queryParam("format", "json")
                .queryParam("limit", limit)
                .queryParam("offset", offset);

        if (StringUtils.isNotBlank(state)) {
            builder.queryParam("filters[state_name]", state.trim());
        }
        if (StringUtils.isNotBlank(finYear)) {
            builder.queryParam("filters[fin_year]", finYear.trim());
        }
        return builder.build(appProperties.getDataGovResourceId());
    }

    private DataGovPage mapResponse(String state, String finYear, int limit, int offset, DataGovResponse response) {
        List<DataGovRecord> records = new ArrayList<>();
        if (response.records() != null) {
            for (Map<String, Object> raw : response.records()) {
                records.add(DataGovRecord.fromRaw(raw));
            }
        }
        boolean hasMore = records.size() == limit;
        return new DataGovPage(state, finYear, offset, limit, records, hasMore);
    }

    public record DataGovPage(
            String state,
            String finYear,
            int offset,
            int limit,
            List<DataGovRecord> records,
            boolean hasMore
    ) {
        public static DataGovPage empty() {
            return new DataGovPage(null, null, 0, 0, List.of(), false);
        }
    }

    public record DataGovRecord(
            String state,
            String district,
            String finYear,
            Integer month,
            Long totalPersondays,
            Long totalHouseholds,
            BigDecimal expenditure,
            Map<String, Object> raw
    ) {
        private static DataGovRecord fromRaw(Map<String, Object> raw) {
            String districtName = normalize(raw.get("district"));
            String stateName = normalize(raw.get("state_name"));
            String finYear = normalize(raw.get("fin_year"));
            Integer month = parseInt(raw.get("month"));
            Long totalPersondays = parseLong(raw.get("persondays_generated"));
            Long totalHouseholds = parseLong(raw.get("no_of_households"));
            BigDecimal expenditure = parseDecimal(raw.get("total_expenditure"));
            return new DataGovRecord(stateName, districtName, finYear, month, totalPersondays, totalHouseholds, expenditure, raw);
        }

        private static String normalize(Object value) {
            return value == null ? null : StringUtils.trimToNull(String.valueOf(value));
        }

        private static Integer parseInt(Object value) {
            if (value == null) {
                return null;
            }
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        private static Long parseLong(Object value) {
            if (value == null) {
                return null;
            }
            try {
                return Long.parseLong(String.valueOf(value));
            } catch (NumberFormatException ex) {
                return null;
            }
        }

        private static BigDecimal parseDecimal(Object value) {
            if (value == null) {
                return null;
            }
            try {
                return new BigDecimal(String.valueOf(value));
            } catch (NumberFormatException ex) {
                return null;
            }
        }
    }

    private record DataGovResponse(List<Map<String, Object>> records) {
    }
}

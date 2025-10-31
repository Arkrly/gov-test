package com.ourvoiceourrights.client;

import com.ourvoiceourrights.config.AppProperties;
import java.util.Map;
import java.util.Optional;
import org.springframework.core.ParameterizedTypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
public class ReverseGeocodeClient {

    private final WebClient.Builder webClientBuilder;
    private final AppProperties appProperties;

    public ReverseGeocodeClient(WebClient.Builder webClientBuilder, AppProperties appProperties) {
        this.webClientBuilder = webClientBuilder;
        this.appProperties = appProperties;
    }

    public Optional<ReverseResult> reverse(double latitude, double longitude) {
        if (StringUtils.isBlank(appProperties.getGeo().getReverseGeocodeBaseUrl())) {
            return Optional.empty();
        }
    try {
        WebClient client = webClientBuilder.clone()
            .baseUrl(appProperties.getGeo().getReverseGeocodeBaseUrl())
            .build();

        Map<String, Object> body = client.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("lat", latitude)
                .queryParam("lon", longitude)
                .build())
            .retrieve()
            .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
            .timeout(appProperties.getGeo().getReverseGeocodeTimeout())
            .blockOptional()
            .orElse(null);
        if (body == null) {
        return Optional.empty();
        }
        return Optional.of(parse(body));
    } catch (Exception exception) {
        log.warn("Reverse geocode lookup failed: {}", exception.getMessage());
        return Optional.empty();
    }
    }

    private ReverseResult parse(Map<String, Object> body) {
        Object addrObj = body.get("address");
        Map<String, Object> address;
        if (addrObj instanceof Map<?, ?> rawMap) {
            java.util.Map<String, Object> tmp = new java.util.HashMap<>();
            for (var e : rawMap.entrySet()) {
                tmp.put(String.valueOf(e.getKey()), e.getValue());
            }
            address = tmp;
        } else {
            address = Map.of();
        }
        String state = extract(address, "state");
        if (state == null) {
            state = extract(address, "state_district");
        }
        String district = extract(address, "county");
        if (district == null) {
            district = extract(address, "city");
        }
        String displayName = (String) body.getOrDefault("display_name", null);
        Double accuracyKm = Optional.ofNullable(body.get("accuracy"))
                .map(Object::toString)
                .map(Double::valueOf)
                .map(value -> value / 1000.0)
                .orElse(5.0);
        return new ReverseResult(displayName, state, district, accuracyKm);
    }

    private String extract(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        return value == null ? null : StringUtils.trimToNull(value.toString());
    }

    public record ReverseResult(String address, String state, String district, Double accuracyKm) {
    }
}

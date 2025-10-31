package com.ourvoiceourrights.service;

import com.ourvoiceourrights.entity.District;
import com.ourvoiceourrights.entity.State;
import com.ourvoiceourrights.repository.DistrictRepository;
import com.ourvoiceourrights.repository.StateRepository;
import com.ourvoiceourrights.service.cache.CacheNames;
import com.ourvoiceourrights.service.cache.CacheService;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DistrictResolutionService {

    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final CacheService cacheService;

    @Transactional
    public District resolveOrCreate(String rawState, String rawDistrict) {
        String stateName = normalize(rawState);
        String districtName = normalize(rawDistrict);
        if (stateName == null || districtName == null) {
            throw new IllegalArgumentException("State and district must be provided");
        }

        State state = cacheService.get(CacheNames.STATES, stateName, State.class)
                .orElseGet(() -> stateRepository.findByNameIgnoreCase(stateName)
                        .orElseGet(() -> createState(stateName)));

        cacheService.put(CacheNames.STATES, stateName, state, java.time.Duration.ofHours(12));

    String key = cacheKey(state.getId(), districtName);
    return cacheService.get(CacheNames.DISTRICTS_BY_STATE, key, District.class)
        .orElseGet(() -> {
            District district = districtRepository.findByStateAndNameIgnoreCase(state, districtName)
                .orElseGet(() -> createDistrict(state, districtName));
            cacheService.put(CacheNames.DISTRICTS_BY_STATE, key, district, java.time.Duration.ofHours(6));
            return district;
        });
    }

    private State createState(String stateName) {
        log.warn("Persisting new state derived from ingestion: {}", stateName);
        State state = State.builder()
                .name(stateName)
                .code(generateCode(stateName))
                .build();
        return stateRepository.save(state);
    }

    private District createDistrict(State state, String districtName) {
        log.warn("Persisting new district derived from ingestion: {} / {}", state.getName(), districtName);
        District district = District.builder()
                .name(districtName)
                .code(generateCode(districtName))
                .state(state)
                .build();
        return districtRepository.save(district);
    }

    private String generateCode(String name) {
    String normalized = Normalizer.normalize(name, Normalizer.Form.NFD)
        .replaceAll("[^\\p{ASCII}]", "");
        normalized = normalized.replaceAll("[^A-Za-z0-9]", "");
        return normalized.length() > 8 ? normalized.substring(0, 8).toUpperCase(Locale.ROOT) : normalized.toUpperCase(Locale.ROOT);
    }

    private String normalize(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        return StringUtils.normalizeSpace(value).toUpperCase(Locale.ROOT);
    }

    private String cacheKey(Long stateId, String districtName) {
        return stateId + "::" + districtName;
    }
}

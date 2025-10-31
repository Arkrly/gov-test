package com.ourvoiceourrights.service;

import com.ourvoiceourrights.config.AppProperties;
import com.ourvoiceourrights.dto.DistrictDto;
import com.ourvoiceourrights.entity.District;
import com.ourvoiceourrights.entity.State;
import com.ourvoiceourrights.repository.DistrictRepository;
import com.ourvoiceourrights.repository.StateRepository;
import com.ourvoiceourrights.service.cache.CacheNames;
import com.ourvoiceourrights.service.cache.CacheService;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DistrictService {

    private final StateRepository stateRepository;
    private final DistrictRepository districtRepository;
    private final CacheService cacheService;
    private final AppProperties appProperties;

    public List<DistrictDto> listDistricts(String stateParam) {
    String key = normalize(stateParam);
    return cacheService.getList(CacheNames.DISTRICTS_BY_STATE, key, DistrictDto.class)
        .orElseGet(() -> {
                    State state = resolveState(stateParam)
                            .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
                    List<DistrictDto> districts = districtRepository.findByStateOrderByNameAsc(state).stream()
                            .sorted(Comparator.comparing(District::getName))
                            .map(district -> new DistrictDto(district.getId(), district.getName(), district.getCode(), state.getId()))
                            .collect(Collectors.toList());
                    cacheService.put(CacheNames.DISTRICTS_BY_STATE, key, districts, appProperties.getCache().getDistrictsTtl());
                    return districts;
                });
    }

    private Optional<State> resolveState(String stateParam) {
        if (StringUtils.isBlank(stateParam)) {
            return Optional.empty();
        }
        String normalized = normalize(stateParam);
        return stateRepository.findByCodeIgnoreCase(normalized)
                .or(() -> stateRepository.findByNameIgnoreCase(normalized));
    }

    private String normalize(String value) {
        return StringUtils.normalizeSpace(value).toUpperCase(Locale.ROOT);
    }
}

package com.ourvoiceourrights.service;

import com.ourvoiceourrights.config.AppProperties;
import com.ourvoiceourrights.dto.PerformanceDto;
import com.ourvoiceourrights.entity.District;
import com.ourvoiceourrights.entity.MgnregaPerformance;
import com.ourvoiceourrights.repository.DistrictRepository;
import com.ourvoiceourrights.repository.MgnregaPerformanceRepository;
import com.ourvoiceourrights.service.cache.CacheNames;
import com.ourvoiceourrights.service.cache.CacheService;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceService {

    private final DistrictRepository districtRepository;
    private final MgnregaPerformanceRepository performanceRepository;
    private final CacheService cacheService;
    private final AppProperties appProperties;

    public PerformanceDto getLatest(String districtRef, String finYear) {
        District district = resolveDistrict(districtRef)
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtRef));
        String cacheKey = district.getId() + "::" + (finYear == null ? "latest" : finYear);

        return cacheService.get(CacheNames.PERFORMANCE_LATEST, cacheKey, PerformanceDto.class)
                .orElseGet(() -> {
            Optional<MgnregaPerformance> performance = StringUtils.isNotBlank(finYear)
                ? performanceRepository.findByDistrictAndFinYearAndMonth(district, finYear, null)
                    .or(() -> performanceRepository.findByDistrictAndFinYearOrderByUpdatedAtDesc(
                        district, finYear, PageRequest.of(0, 1)).getContent().stream().findFirst())
                            : performanceRepository.findTopByDistrictOrderByUpdatedAtDesc(district);
                    PerformanceDto dto = performance
                            .map(this::toDto)
                            .orElseThrow(() -> new IllegalArgumentException("No performance data for district"));
                    cacheService.put(CacheNames.PERFORMANCE_LATEST, cacheKey, dto, appProperties.getCache().getLatestPerformanceTtl());
                    return dto;
                });
    }

    public List<PerformanceDto> getHistory(String districtRef, String finYear, int limit, int offset, Instant from, Instant to) {
        District district = resolveDistrict(districtRef)
                .orElseThrow(() -> new IllegalArgumentException("District not found: " + districtRef));
        String cacheKey = String.join("::",
                district.getId().toString(),
                Optional.ofNullable(finYear).orElse("*"),
                String.valueOf(limit),
                String.valueOf(offset),
                Optional.ofNullable(from).map(Instant::toEpochMilli).map(Object::toString).orElse(""),
                Optional.ofNullable(to).map(Instant::toEpochMilli).map(Object::toString).orElse(""));

    return cacheService.getList(CacheNames.PERFORMANCE_HISTORY, cacheKey, PerformanceDto.class)
        .orElseGet(() -> {
                    Pageable pageable = PageRequest.of(offset / Math.max(limit, 1), Math.max(limit, 1));
                    Page<MgnregaPerformance> page = StringUtils.isNotBlank(finYear)
                            ? performanceRepository.findByDistrictAndFinYearOrderByUpdatedAtDesc(district, finYear, pageable)
                            : performanceRepository.findByDistrictOrderByUpdatedAtDesc(district, pageable);

                    List<PerformanceDto> history = page.stream()
                            .filter(perf -> filterByRange(perf, from, to))
                            .sorted(Comparator.comparing(MgnregaPerformance::getUpdatedAt).reversed())
                            .map(this::toDto)
                            .collect(Collectors.toList());
                    cacheService.put(CacheNames.PERFORMANCE_HISTORY, cacheKey, history, appProperties.getCache().getHistoryTtl());
                    return history;
                });
    }

    private boolean filterByRange(MgnregaPerformance performance, Instant from, Instant to) {
        Instant updated = performance.getUpdatedAt();
        if (from != null && updated.isBefore(from)) {
            return false;
        }
        if (to != null && updated.isAfter(to)) {
            return false;
        }
        return true;
    }

    private Optional<District> resolveDistrict(String reference) {
        if (StringUtils.isBlank(reference)) {
            return Optional.empty();
        }
    if (reference.chars().allMatch(Character::isDigit)) {
            return districtRepository.findById(Long.parseLong(reference));
        }
    String normalized = StringUtils.normalizeSpace(reference);
    return districtRepository.findByCodeIgnoreCase(normalized)
        .or(() -> districtRepository.findByNameIgnoreCase(normalized));
    }

    private PerformanceDto toDto(MgnregaPerformance entity) {
    return new PerformanceDto(
        entity.getDistrict().getName(),
        entity.getDistrict().getState().getName(),
        entity.getFinYear(),
        entity.getMonth(),
        entity.getTotalPersondays(),
        entity.getTotalHouseholds(),
        entity.getExpenditure(),
        entity.getUpdatedAt()
    );
    }
}

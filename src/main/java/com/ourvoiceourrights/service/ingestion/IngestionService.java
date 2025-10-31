package com.ourvoiceourrights.service.ingestion;

import com.ourvoiceourrights.client.DataGovClient;
import com.ourvoiceourrights.config.AppProperties;
import com.ourvoiceourrights.entity.District;
import com.ourvoiceourrights.entity.MgnregaPerformance;
import com.ourvoiceourrights.entity.State;
import com.ourvoiceourrights.mapper.IngestionMapper;
import com.ourvoiceourrights.repository.MgnregaPerformanceRepository;
import com.ourvoiceourrights.repository.StateRepository;
import com.ourvoiceourrights.service.DistrictResolutionService;
import com.ourvoiceourrights.service.cache.CacheNames;
import com.ourvoiceourrights.service.cache.CacheService;
import java.time.Instant;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final DataGovClient dataGovClient;
    private final AppProperties appProperties;
    private final DistrictResolutionService districtResolutionService;
    private final MgnregaPerformanceRepository performanceRepository;
    private final StateRepository stateRepository;
    private final CacheService cacheService;

    @Transactional
    public void ingestAllConfiguredStates() {
        List<String> targetStates = resolveTargetStates();
        List<String> targetFinancialYears = resolveFinancialYears();
        Instant ingestStart = Instant.now();

        for (String state : targetStates) {
            for (String finYear : targetFinancialYears) {
                ingestStateYear(state, finYear, ingestStart);
            }
        }
    }

    private List<String> resolveTargetStates() {
        Set<String> states = new HashSet<>();
        appProperties.getIngestion().getDefaultStates().forEach(value -> states.add(value.trim().toUpperCase()));
        stateRepository.findAll().stream()
                .map(State::getName)
                .filter(StringUtils::isNotBlank)
                .map(value -> value.trim().toUpperCase())
                .forEach(states::add);
        return new ArrayList<>(states);
    }

    private List<String> resolveFinancialYears() {
        List<String> years = new ArrayList<>();
        int rolling = appProperties.getIngestion().getRollingFinancialYears();
        Year currentFinancialYear = determineFinancialYear();
        for (int i = 0; i < rolling; i++) {
            Year year = currentFinancialYear.minusYears(i);
            String formatted = formatFinancialYear(year);
            years.add(formatted);
        }
        return years;
    }

    private Year determineFinancialYear() {
        Instant now = Instant.now();
        java.time.ZonedDateTime zoned = now.atZone(java.time.ZoneOffset.UTC);
        int year = zoned.getYear();
        Month month = zoned.getMonth();
        if (month.getValue() < 4) { // Financial year starts in April
            year = year - 1;
        }
        return Year.of(year);
    }

    private String formatFinancialYear(Year startYear) {
        return startYear.getValue() + "-" + startYear.plusYears(1).getValue();
    }

    private void ingestStateYear(String state, String finYear, Instant ingestStart) {
        log.info("Ingesting DataGov metrics state={} finYear={}", state, finYear);
        AtomicInteger processed = new AtomicInteger();
        AtomicInteger upserts = new AtomicInteger();
    int pageSize = Math.min(appProperties.getIngestion().getPaginationStep(), appProperties.getIngestion().getMaxFetchLimit());
    int limit = Math.max(1, pageSize);

        int offset = 0;
        boolean hasMore;
        do {
            DataGovClient.DataGovPage page = dataGovClient.fetchMetrics(state, finYear, limit, offset);
            page.records().forEach(record -> {
                processed.incrementAndGet();
                if (processRecord(record, ingestStart)) {
                    upserts.incrementAndGet();
                }
            });
            offset += limit;
            hasMore = page.hasMore();
            log.info("Ingested page state={} finYear={} offset={} processed={} upserts={}", state, finYear, offset, processed.get(), upserts.get());
        } while (hasMore);

        log.info("Completed ingestion state={} finYear={} processed={} upserts={} latencyMs={}",
                state, finYear, processed.get(), upserts.get(), java.time.Duration.between(ingestStart, Instant.now()).toMillis());
    }

    private boolean processRecord(DataGovClient.DataGovRecord record, Instant ingestedAt) {
        if (record == null || record.district() == null) {
            return false;
        }

        District district = districtResolutionService.resolveOrCreate(record.state(), record.district());
    String finYear = record.finYear() == null
        ? formatFinancialYear(determineFinancialYear())
        : record.finYear().toUpperCase();
        Integer month = record.month();
        String sourceHash = IngestionMapper.sourceHash(record);

        MgnregaPerformance existing = performanceRepository
                .findByDistrictAndFinYearAndMonth(district, finYear, month)
                .orElse(null);

        if (existing != null) {
            if (sourceHash.equals(existing.getSourceHash())) {
                return false;
            }
            existing.setTotalHouseholds(record.totalHouseholds());
            existing.setTotalPersondays(record.totalPersondays());
            existing.setExpenditure(record.expenditure());
            existing.setSourceHash(sourceHash);
            existing.setUpdatedAt(Instant.now());
            performanceRepository.save(existing);
        } else {
            MgnregaPerformance entity = IngestionMapper.toEntity(record, district, ingestedAt);
            entity.setFinYear(finYear);
            entity.setMonth(month);
            entity.setSourceHash(sourceHash);
            performanceRepository.save(entity);
        }

        cacheService.clear(CacheNames.PERFORMANCE_LATEST);
        cacheService.clear(CacheNames.PERFORMANCE_HISTORY);
        return true;
    }
}

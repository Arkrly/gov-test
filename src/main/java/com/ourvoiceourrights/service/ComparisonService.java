package com.ourvoiceourrights.service;

import com.ourvoiceourrights.dto.CompareDto;
import com.ourvoiceourrights.dto.PerformanceDto;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComparisonService {

    private final PerformanceService performanceService;

    public CompareDto compare(String districtOne, String districtTwo) {
        PerformanceDto left = performanceService.getLatest(districtOne, null);
        PerformanceDto right = performanceService.getLatest(districtTwo, null);

        Long personDaysDelta = delta(left.totalPersondays(), right.totalPersondays());
        Long householdsDelta = delta(left.totalHouseholds(), right.totalHouseholds());
        BigDecimal expenditureDelta = delta(left.expenditure(), right.expenditure());

        return new CompareDto(left, right, personDaysDelta, householdsDelta, expenditureDelta);
    }

    private Long delta(Long left, Long right) {
        if (left == null && right == null) {
            return null;
        }
        return (left == null ? 0 : left) - (right == null ? 0 : right);
    }

    private BigDecimal delta(BigDecimal left, BigDecimal right) {
        if (left == null && right == null) {
            return null;
        }
        return (left == null ? BigDecimal.ZERO : left).subtract(right == null ? BigDecimal.ZERO : right);
    }
}

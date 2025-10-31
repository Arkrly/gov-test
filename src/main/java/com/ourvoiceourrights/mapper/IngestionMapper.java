package com.ourvoiceourrights.mapper;

import com.ourvoiceourrights.client.DataGovClient;
import com.ourvoiceourrights.entity.District;
import com.ourvoiceourrights.entity.MgnregaPerformance;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import org.apache.commons.codec.digest.DigestUtils;

public final class IngestionMapper {

    private IngestionMapper() {
    }

    public static MgnregaPerformance toEntity(DataGovClient.DataGovRecord record, District district, Instant ingestedAt) {
        return MgnregaPerformance.builder()
                .district(district)
                .finYear(normalizeFinYear(record.finYear()))
                .month(record.month())
                .totalPersondays(record.totalPersondays())
                .totalHouseholds(record.totalHouseholds())
                .expenditure(normalizeDecimal(record.expenditure()))
                .sourceHash(sourceHash(record))
                .ingestedAt(ingestedAt)
                .updatedAt(ingestedAt)
                .build();
    }

    public static String sourceHash(DataGovClient.DataGovRecord record) {
        String payload = String.join("|",
                Objects.toString(record.state(), ""),
                Objects.toString(record.district(), ""),
                Objects.toString(normalizeFinYear(record.finYear()), ""),
                Objects.toString(record.month(), ""),
                Objects.toString(record.totalPersondays(), ""),
                Objects.toString(record.totalHouseholds(), ""),
                Objects.toString(normalizeDecimal(record.expenditure()), ""));
        return DigestUtils.sha256Hex(payload);
    }

    private static String normalizeFinYear(String finYear) {
        if (finYear == null) {
            return null;
        }
        return finYear.trim().replaceAll("\\s+", "").toUpperCase();
    }

    private static BigDecimal normalizeDecimal(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}

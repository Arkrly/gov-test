package com.ourvoiceourrights.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ourvoiceourrights.client.DataGovClient;
import com.ourvoiceourrights.entity.District;
import com.ourvoiceourrights.entity.State;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IngestionMapperTest {

    @Test
    void shouldProduceStableSourceHash() {
        DataGovClient.DataGovRecord record = new DataGovClient.DataGovRecord(
                "KARNATAKA",
                "BENGALURU",
                "2023-2024",
                5,
                1000L,
                400L,
                new BigDecimal("12345.67"),
                Map.of("district", "BENGALURU")
        );

        String hash1 = IngestionMapper.sourceHash(record);
        String hash2 = IngestionMapper.sourceHash(record);

        assertThat(hash1).isEqualTo(hash2).hasSize(64);
    }

    @Test
    void shouldMapToEntity() {
        State state = State.builder().id(1L).name("KARNATAKA").code("KA").build();
        District district = District.builder().id(10L).name("BENGALURU").state(state).code("BEN").build();
        DataGovClient.DataGovRecord record = new DataGovClient.DataGovRecord(
                "KARNATAKA",
                "BENGALURU",
                "2023-2024",
                6,
                2000L,
                800L,
                new BigDecimal("54321.00"),
                Map.of()
        );

        var entity = IngestionMapper.toEntity(record, district, Instant.parse("2024-01-01T00:00:00Z"));

        assertThat(entity.getDistrict()).isEqualTo(district);
        assertThat(entity.getSourceHash()).isNotBlank();
        assertThat(entity.getFinYear()).isEqualTo("2023-2024");
        assertThat(entity.getMonth()).isEqualTo(6);
    }
}

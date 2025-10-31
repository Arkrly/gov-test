package com.ourvoiceourrights.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ourvoiceourrights.dto.PerformanceDto;
import com.ourvoiceourrights.service.PerformanceService;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PerformanceController.class)
class PerformanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PerformanceService performanceService;

    @Test
    void shouldRejectInvalidLimit() throws Exception {
        mockMvc.perform(get("/api/performance/10/history").param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnLatestPerformance() throws Exception {
        PerformanceDto dto = new PerformanceDto("BENGALURU", "KARNATAKA", "2023-2024", 5,
                1_000L, 500L, new BigDecimal("123.45"), Instant.now());
        when(performanceService.getLatest("10", null)).thenReturn(dto);

        mockMvc.perform(get("/api/performance/10"))
                .andExpect(status().isOk());
    }
}

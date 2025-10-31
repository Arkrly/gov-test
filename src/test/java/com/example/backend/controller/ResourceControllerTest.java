package com.example.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.data-gov.api-key=579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b")
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnMockPayload() throws Exception {
        mockMvc.perform(get("/resource/ee03643a-ee4c-48c2-ac30-9f2ff26ab722")
                        .param("api-key", "579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b")
                        .param("format", "json")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceId", is("ee03643a-ee4c-48c2-ac30-9f2ff26ab722")))
                .andExpect(jsonPath("$.query.limit", is(10)));
    }

    @Test
    void shouldRejectUnsupportedFormat() throws Exception {
        mockMvc.perform(get("/resource/ee03643a-ee4c-48c2-ac30-9f2ff26ab722")
                        .param("api-key", "579b464db66ec23bdd000001cdd3946e44ce4aad7209ff7b23ac571b")
                        .param("format", "yaml"))
                .andExpect(status().isBadRequest());
    }
}

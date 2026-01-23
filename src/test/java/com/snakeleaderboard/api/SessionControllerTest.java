package com.snakeleaderboard.api;

import com.snakeleaderboard.config.RateLimitFilter;
import com.snakeleaderboard.dto.StartSessionResponse;
import com.snakeleaderboard.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = SessionController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class
        )
)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionService sessionService;

    @Test
    void startSession_returnsSessionPayload() throws Exception {
        UUID sessionId = UUID.randomUUID();
        StartSessionResponse response = new StartSessionResponse(
                sessionId,
                "token123",
                Instant.parse("2026-01-20T12:34:56Z")
        );

        when(sessionService.createSession()).thenReturn(response);

        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.sessionToken").value("token123"))
                .andExpect(jsonPath("$.expiresAt").value("2026-01-20T12:34:56Z"));
    }
}

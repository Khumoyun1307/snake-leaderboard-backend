package com.snakeleaderboard.api;

import com.snakeleaderboard.config.RateLimitFilter;
import com.snakeleaderboard.dto.SubmitScoreRequest;
import com.snakeleaderboard.error.ApiExceptionHandler;
import com.snakeleaderboard.service.ScoreService;
import com.snakeleaderboard.service.SessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ScoreController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class
        )
)
@Import(ApiExceptionHandler.class)
class ScoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SessionService sessionService;

    @MockitoBean
    private ScoreService scoreService;

    @Test
    void submitScore_returns201WhenSessionValid() throws Exception {
        UUID sessionId = UUID.randomUUID();
        UUID scoreId = UUID.randomUUID();

        when(sessionService.isValidSession(eq(sessionId), anyString())).thenReturn(true);
        when(scoreService.saveScore(any())).thenReturn(scoreId);

        mockMvc.perform(post("/api/scores")
                        .header("X-Session-Id", sessionId)
                        .header("X-Session-Token", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scoreId").value(scoreId.toString()));
    }

    @Test
    void submitScore_returns401WhenSessionInvalid() throws Exception {
        UUID sessionId = UUID.randomUUID();

        when(sessionService.isValidSession(eq(sessionId), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/scores")
                        .header("X-Session-Id", sessionId)
                        .header("X-Session-Token", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid or expired session"));
    }

    @Test
    void submitScore_returns400WhenMissingHeaders() throws Exception {
        mockMvc.perform(post("/api/scores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("X-Session-Id")));
    }

    @Test
    void submitScore_missingTokenHeader_returns400() throws Exception {
        UUID sessionId = UUID.randomUUID();

        mockMvc.perform(post("/api/scores")
                        .header("X-Session-Id", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("X-Session-Token")));
    }

    @Test
    void submitScore_returns400WhenValidationFails() throws Exception {
        UUID sessionId = UUID.randomUUID();

        when(sessionService.isValidSession(eq(sessionId), anyString())).thenReturn(true);

        SubmitScoreRequest invalid = new SubmitScoreRequest(
                UUID.randomUUID(),
                "",
                10,
                1,
                "MAP_SELECT",
                "NORMAL",
                1000L,
                "1.0.0"
        );

        mockMvc.perform(post("/api/scores")
                        .header("X-Session-Id", sessionId)
                        .header("X-Session-Token", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("playerName")));
    }

    @Test
    void submitScore_invalidScore_returns400() throws Exception {
        UUID sessionId = UUID.randomUUID();

        when(sessionService.isValidSession(eq(sessionId), anyString())).thenReturn(true);

        SubmitScoreRequest invalid = new SubmitScoreRequest(
                UUID.randomUUID(),
                "player1",
                2_000_001,
                1,
                "MAP_SELECT",
                "NORMAL",
                1000L,
                "1.0.0"
        );

        mockMvc.perform(post("/api/scores")
                        .header("X-Session-Id", sessionId)
                        .header("X-Session-Token", "token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("score")));
    }

    private SubmitScoreRequest validRequest() {
        return new SubmitScoreRequest(
                UUID.randomUUID(),
                "player1",
                42,
                2,
                "MAP_SELECT",
                "NORMAL",
                26000L,
                "1.0.0"
        );
    }
}

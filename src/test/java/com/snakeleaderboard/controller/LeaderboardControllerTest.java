package com.snakeleaderboard.controller;

import com.snakeleaderboard.config.RateLimitFilter;
import com.snakeleaderboard.dto.LeaderboardEntry;
import com.snakeleaderboard.service.LeaderboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = LeaderboardController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = RateLimitFilter.class
        )
)
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LeaderboardService leaderboardService;

    @Test
    void leaderboard_returnsEntries() throws Exception {
        List<LeaderboardEntry> entries = List.of(
                new LeaderboardEntry(
                        1,
                        "player1",
                        2,
                        "NORMAL",
                        100,
                        25000L,
                        Instant.parse("2026-01-20T12:00:00Z")
                )
        );

        when(leaderboardService.getTop(eq(2), eq("MAP_SELECT"), eq("NORMAL"), eq(10), eq(0)))
                .thenReturn(entries);

        mockMvc.perform(get("/api/leaderboard")
                        .param("mapId", "2")
                        .param("mode", "MAP_SELECT")
                        .param("difficulty", "NORMAL")
                        .param("limit", "10")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mapId").value(2))
                .andExpect(jsonPath("$.mode").value("MAP_SELECT"))
                .andExpect(jsonPath("$.difficulty").value("NORMAL"))
                .andExpect(jsonPath("$.entries[0].playerName").value("player1"))
                .andExpect(jsonPath("$.entries[0].score").value(100));
    }

    @Test
    void leaderboard_missingMapId_returns400() throws Exception {
        mockMvc.perform(get("/api/leaderboard")
                        .param("mode", "MAP_SELECT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void leaderboard_missingMode_returns400() throws Exception {
        mockMvc.perform(get("/api/leaderboard")
                        .param("mapId", "2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void leaderboard_negativeMapId_returns400() throws Exception {
        mockMvc.perform(get("/api/leaderboard")
                        .param("mapId", "-1")
                        .param("mode", "MAP_SELECT"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void leaderboard_limitTooHigh_returns400() throws Exception {
        mockMvc.perform(get("/api/leaderboard")
                        .param("mapId", "2")
                        .param("mode", "MAP_SELECT")
                        .param("limit", "51"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void leaderboard_negativeOffset_returns400() throws Exception {
        mockMvc.perform(get("/api/leaderboard")
                        .param("mapId", "2")
                        .param("mode", "MAP_SELECT")
                        .param("offset", "-1"))
                .andExpect(status().isBadRequest());
    }
}

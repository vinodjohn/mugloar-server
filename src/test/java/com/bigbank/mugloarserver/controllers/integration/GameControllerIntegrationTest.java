package com.bigbank.mugloarserver.controllers.integration;

import com.bigbank.mugloarserver.models.GameResult;
import com.bigbank.mugloarserver.services.GameResultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Game Controller
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
@SpringBootTest
@AutoConfigureMockMvc
class GameControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameResultService gameResultService;

    @Test
    void startGame_ReturnsGameId() throws Exception {
        mockMvc.perform(post("/game/start"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("gameId")));
    }

    @Test
    void displayResult_Valid() throws Exception {
        GameResult gr = new GameResult();
        gr.setGameId("testIntegrationId");
        gameResultService.save(gr);
        mockMvc.perform(get("/game/testIntegrationId"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"));
    }

    @Test
    void displayResult_Invalid() throws Exception {
        mockMvc.perform(get("/game/invalidGameIdIntegration"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("Failed to retrieve game results.")));
    }

    @Test
    void getGameHistory_Success() throws Exception {
        mockMvc.perform(get("/game/history?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(view().name("history"));
    }

    @Test
    void getGameHistory_NotFoundError() throws Exception {
        mockMvc.perform(get("/game/history?error=notfound"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("Failed to retrieve game history.")));
    }
}

package com.bigbank.mugloarserver.controllers.integration;

import com.bigbank.mugloarserver.facades.GameFacade;
import com.bigbank.mugloarserver.models.Game;
import com.bigbank.mugloarserver.models.GameResult;
import com.bigbank.mugloarserver.services.GameResultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for GameController
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(GameControllerIntegrationTest.TestConfig.class)
public class GameControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GameResultService gameResultService;
    @Autowired
    private GameFacade gameFacade;

    @Test
    void startGame_ReturnsGameId() throws Exception {
        mockMvc.perform(post("/game/start"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"gameId\": \"testId\"")));
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

    @TestConfiguration
    static class TestConfig {
        @Bean
        public GameFacade gameFacade() {
            GameFacade mockFacade = mock(GameFacade.class);
            Game mockGame = new Game();
            mockGame.setGameId("testId");
            when(mockFacade.initializeGame()).thenReturn(mockGame);
            return mockFacade;
        }
    }
}

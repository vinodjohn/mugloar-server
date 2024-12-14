package com.bigbank.mugloarserver.controllers.unit;

import com.bigbank.mugloarserver.controllers.GameController;
import com.bigbank.mugloarserver.facades.GameFacade;
import com.bigbank.mugloarserver.models.Game;
import com.bigbank.mugloarserver.models.GameResult;
import com.bigbank.mugloarserver.services.GameResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GameController
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
public class GameControllerTest {
    @InjectMocks
    private GameController gameController;

    @Mock
    private GameFacade gameFacade;

    @Mock
    private GameResultService gameResultService;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void startGame_SuccessfulInitialization() {
        Game mockGame = new Game();
        mockGame.setGameId("test123");
        when(gameFacade.initializeGame()).thenReturn(mockGame);
        ResponseEntity<?> response = gameController.startGame();
        assertEquals(200, response.getStatusCode().value());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("test123"));
        verify(gameFacade).initializeGame();
    }

    @Test
    void startGame_FailedInitialization() {
        when(gameFacade.initializeGame()).thenReturn(null);
        ResponseEntity<?> response = gameController.startGame();
        assertEquals(500, response.getStatusCode().value());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Failed to start the game"));
    }

    @Test
    void displayResult_ValidGameId() {
        GameResult mockResult = new GameResult(null, "validId", 0, 0, 3, 100.0, 1, 0, false, null, null);
        when(gameResultService.findByGameId("validId")).thenReturn(mockResult);
        String view = gameController.displayResult(model, "validId");
        assertEquals("result", view);
        verify(model).addAttribute("gameResult", mockResult);
        verify(model).addAttribute("processedMessages", mockResult.getProcessedMessages());
    }

    @Test
    void displayResult_InvalidGameId() {
        when(gameResultService.findByGameId("invalidId")).thenReturn(null);
        String view = gameController.displayResult(model, "invalidId");
        assertEquals("error", view);
        verify(model).addAttribute("errorMessage", "Failed to retrieve game results.");
    }

    @Test
    void getGameHistory_Success() {
        gameController.getGameHistory(0, 10, null, model);
        verify(gameResultService).getAllGameResults(any());
    }

    @Test
    void getGameHistory_NotFoundError() {
        String view = gameController.getGameHistory(0, 10, "notfound", model);
        assertEquals("error", view);
        verify(model).addAttribute("errorMessage", "Failed to retrieve game history.");
    }

    @SuppressWarnings("unchecked")
    @Test
    void startGame_ActiveGamesMapUpdated() {
        Game mockGame = new Game();
        mockGame.setGameId("game123");
        when(gameFacade.initializeGame()).thenReturn(mockGame);

        gameController.startGame();

        ConcurrentHashMap<String, Game> activeGames =
                (ConcurrentHashMap<String, Game>) ReflectionTestUtils.getField(gameController, "activeGames");

        assertNotNull(activeGames);
        assertFalse(activeGames.containsKey("game123"));
    }
}
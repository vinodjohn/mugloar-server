package com.bigbank.mugloarserver.facades.unit;

import com.bigbank.mugloarserver.exceptions.GameOverException;
import com.bigbank.mugloarserver.exceptions.MugloarException;
import com.bigbank.mugloarserver.facades.GameFacade;
import com.bigbank.mugloarserver.models.*;
import com.bigbank.mugloarserver.services.GameResultService;
import com.bigbank.mugloarserver.services.MugloarService;
import com.bigbank.mugloarserver.services.StrategyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GameFacade
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
public class GameFacadeTest {
    @InjectMocks
    private GameFacade gameFacade;

    @Mock
    private MugloarService mugloarService;

    @Mock
    private StrategyService strategyService;

    @Mock
    private GameResultService gameResultService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private Game testGame;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testGame = new Game();
        testGame.setGameId("test123");
        testGame.setLives(3);
        testGame.setGold(100.0);
        testGame.setLevel(1);
        testGame.setScore(0);
        testGame.setTurn(0);
    }

    @Test
    void initializeGame_Successful() {
        when(mugloarService.startGame()).thenReturn(testGame);

        Game resultGame = gameFacade.initializeGame();

        assertNotNull(resultGame);
        assertEquals("test123", resultGame.getGameId());
        verify(mugloarService).startGame();
        verify(messagingTemplate).convertAndSend(
                eq("/topic/game-status/test123"), any(GameStateMessage.class));
    }

    @Test
    void initializeGame_GameOverException() {
        when(mugloarService.startGame()).thenThrow(new GameOverException("Game Over"));

        assertThrows(GameOverException.class, () -> gameFacade.initializeGame());
    }

    @Test
    void initializeGame_UnhandledException() {
        when(mugloarService.startGame()).thenThrow(new RuntimeException("Unexpected Error"));

        Game resultGame = gameFacade.initializeGame();

        assertNull(resultGame);
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void playGame_NullGame() {
        assertDoesNotThrow(() -> gameFacade.playGame(null));
        verifyNoInteractions(mugloarService);
    }

    @Test
    void playGame_GameOverDetected() {
        doThrow(new GameOverException("Game Over"))
                .when(mugloarService).investigate("test123");

        assertDoesNotThrow(() -> gameFacade.playGame(testGame));
        verify(messagingTemplate, atLeastOnce()).convertAndSend(
                contains("/topic/game-status/test123"), any(GameStateMessage.class));
    }

    @Test
    void performInvestigation_Success() {
        Investigation investigation = new Investigation();
        when(mugloarService.investigate("test123")).thenReturn(investigation);

        boolean result = gameFacade.performInvestigation(testGame);

        assertTrue(result);
        verify(strategyService).processInvestigation(investigation);
    }

    @Test
    void performInvestigation_FailsWithException() {
        when(mugloarService.investigate("test123")).thenThrow(new MugloarException("Error"));

        boolean result = gameFacade.performInvestigation(testGame);

        assertFalse(result);
        verify(strategyService, never()).processInvestigation(any());
    }

    @Test
    void performMessageSolving_Success() {
        Message message = new Message("ad1", "Message Text", "50", 2, null, "High");
        MessageSolveResponse response = new MessageSolveResponse(true, 3, 150.0, 300, 1, 1, "Success");

        when(mugloarService.getMessages("test123")).thenReturn(List.of(message));
        when(strategyService.chooseMessage(anyList(), eq(testGame))).thenReturn(message);
        when(mugloarService.solveMessage("test123", "ad1")).thenReturn(response);

        List<ProcessedMessage> processedMessages = new ArrayList<>();
        boolean result = gameFacade.performMessageSolving(testGame, processedMessages);

        assertTrue(result);
        assertEquals(1, processedMessages.size());
        verify(strategyService).markMessageAsSolved("ad1");
    }

    @Test
    void performMessageSolving_Failed() {
        Message message = new Message("ad1", "Message Text", "50", 2, null, "Low");
        when(mugloarService.getMessages("test123")).thenReturn(List.of(message));
        when(strategyService.chooseMessage(anyList(), eq(testGame))).thenReturn(message);
        when(mugloarService.solveMessage("test123", "ad1"))
                .thenReturn(new MessageSolveResponse(false, 2, 100.0, 0, 0, 0, "Failed"));

        List<ProcessedMessage> processedMessages = new ArrayList<>();
        boolean result = gameFacade.performMessageSolving(testGame, processedMessages);

        assertFalse(result);
        assertEquals(1, processedMessages.size());
        verify(strategyService).recordFailure("ad1");
    }

    @Test
    void performShopPhase_ItemsBoughtSuccessfully() {
        ShopItem item = new ShopItem("hpot", "Healing Potion", 50.0);
        ShopPurchaseResponse purchaseResponse = new ShopPurchaseResponse("success", 90.0, 3, 1, 1);

        when(mugloarService.getShopItems("test123")).thenReturn(List.of(item));
        when(strategyService.decideItemsToBuy(testGame, List.of(item))).thenReturn(List.of(item));
        when(mugloarService.buyItem("test123", "hpot")).thenReturn(purchaseResponse);

        assertDoesNotThrow(() -> gameFacade.performShopPhase(testGame));
    }

    @Test
    void finalizeGame_SavesSuccessfully() {
        testGame.setScore(1200);
        when(gameResultService.findByGameId("test123")).thenReturn(null);

        gameFacade.finalizeGame(testGame, new ArrayList<>());
        verify(gameResultService).save(any(GameResult.class));
    }

    @Test
    void finalizeGame_DuplicateGameResult() {
        when(gameResultService.findByGameId("test123")).thenReturn(new GameResult());

        gameFacade.finalizeGame(testGame, new ArrayList<>());
        verify(gameResultService, never()).save(any());
    }

    @Test
    void terminateGame_NullGame() {
        assertDoesNotThrow(() -> gameFacade.terminateGame(null, Collections.emptyList(), "Reason"));
        verify(messagingTemplate, atLeastOnce()).convertAndSend(
                contains("/topic/game-status/Unknown"), Optional.ofNullable(any()));
    }

    @Test
    void terminateGame_Successful() {
        assertDoesNotThrow(() -> gameFacade.terminateGame(testGame, new ArrayList<>(), "Game ended"));
        verify(messagingTemplate, atLeastOnce()).convertAndSend(
                contains("/topic/game-status/test123"), Optional.ofNullable(any()));
    }
}
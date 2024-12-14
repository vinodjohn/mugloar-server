package com.bigbank.mugloarserver.facades.unit;

import com.bigbank.mugloarserver.exceptions.DuplicateGameResultException;
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
class GameFacadeTest {

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
        testGame.setHighScore(0);
        testGame.setTurn(0);
        testGame.setWingStrength(0);
        testGame.setScaleThickness(0);
        testGame.setFireBreath(0);
        testGame.setCunning(0);
        testGame.setClawSharpness(0);
    }

    @Test
    void initializeGame_Successful() {
        when(mugloarService.startGame()).thenReturn(testGame);

        Game resultGame = gameFacade.initializeGame();

        assertNotNull(resultGame);
        assertEquals("test123", resultGame.getGameId());
        verify(mugloarService, times(1)).startGame();
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/game-status/test123"),
                any(GameStateMessage.class));
    }

    @Test
    void initializeGame_GameOver() {
        when(mugloarService.startGame()).thenThrow(new GameOverException("Game Over"));

        assertThrows(GameOverException.class, () -> gameFacade.initializeGame());
    }

    @Test
    void initializeGame_MugloarException() {
        when(mugloarService.startGame()).thenThrow(new MugloarException("Some Mugloar Error"));

        Game resultGame = gameFacade.initializeGame();
        assertNull(resultGame);
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void initializeGame_UnexpectedException() {
        when(mugloarService.startGame()).thenThrow(new RuntimeException("Unexpected"));

        Game resultGame = gameFacade.initializeGame();
        assertNull(resultGame);
        verify(messagingTemplate, never()).convertAndSend(anyString(), Optional.ofNullable(any()));
    }

    @Test
    void playGame_GameNull() {
        assertDoesNotThrow(() -> gameFacade.playGame(null));
        verifyNoInteractions(mugloarService);
    }

    @Test
    void playGame_LoopRunsAndExitsOnGameOver() {
        doThrow(new GameOverException("Game Over"))
                .when(mugloarService).investigate("test123");

        assertDoesNotThrow(() -> gameFacade.playGame(testGame));
        verify(messagingTemplate, atLeastOnce()).convertAndSend(contains("/topic/game-status/test123"),
                any(GameStateMessage.class));
    }

    @Test
    void playGame_LoopHandlesMugloarExceptionAndContinues() {
        doThrow(new MugloarException("Mugloar Error"))
                .doThrow(new GameOverException("Game Over"))
                .when(mugloarService).investigate("test123");

        when(mugloarService.getMessages("test123"))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> gameFacade.playGame(testGame));
        verify(mugloarService, atLeast(2)).investigate("test123");
        verify(messagingTemplate, atLeastOnce())
                .convertAndSend(eq("/topic/game-status/test123"), any(GameStateMessage.class));
    }

    @Test
    void performInvestigation_Success() {
        Investigation mockInvestigation = new Investigation();
        when(mugloarService.investigate("test123")).thenReturn(mockInvestigation);

        boolean result = gameFacade.performInvestigation(testGame);

        assertTrue(result);
        verify(strategyService, times(1)).processInvestigation(mockInvestigation);
        verify(messagingTemplate, never()).convertAndSend(anyString(), contains("error"));
    }

    @Test
    void performInvestigation_GameOverException() {
        doThrow(new GameOverException("Game Over")).when(mugloarService).investigate("test123");

        assertThrows(GameOverException.class, () -> gameFacade.performInvestigation(testGame));
        verify(strategyService, never()).processInvestigation(any());
    }

    @Test
    void performInvestigation_MugloarException() {
        doThrow(new MugloarException("Error")).when(mugloarService).investigate("test123");
        boolean result = gameFacade.performInvestigation(testGame);

        assertFalse(result);
        verify(strategyService, never()).processInvestigation(any());
    }

    @Test
    void performInvestigation_NullInvestigation() {
        when(mugloarService.investigate("test123")).thenReturn(null);
        boolean result = gameFacade.performInvestigation(testGame);

        assertFalse(result);
        verify(strategyService, never()).processInvestigation(any());
    }

    @Test
    void performMessageSolving_NoMessages() {
        when(mugloarService.getMessages("test123")).thenReturn(Collections.emptyList());
        List<ProcessedMessage> pmList = new ArrayList<>();

        boolean result = gameFacade.performMessageSolving(testGame, pmList);

        assertFalse(result);
        assertTrue(pmList.isEmpty());
    }

    @Test
    void performMessageSolving_SuccessfulSolve() {
        Message mockMessage = new Message("ad1", "message1", "50", 1, null, "", Collections.emptyList(), "");
        MessageSolveResponse solveResp = new MessageSolveResponse(true, 3, 150.0, 50, 0, 0, "solved");
        when(mugloarService.getMessages("test123")).thenReturn(List.of(mockMessage));
        when(strategyService.chooseMessage(anyList(), eq(testGame))).thenReturn(mockMessage);
        when(mugloarService.solveMessage("test123", "ad1")).thenReturn(solveResp);

        List<ProcessedMessage> pmList = new ArrayList<>();
        boolean result = gameFacade.performMessageSolving(testGame, pmList);

        assertTrue(result);
        assertEquals(1, pmList.size());
        verify(strategyService).markMessageAsSolved("ad1");
    }

    @Test
    void performMessageSolving_FailedSolve() {
        Message mockMessage = new Message("ad1", "message1", "50", 1, null, "", Collections.emptyList(), "");
        when(mugloarService.getMessages("test123")).thenReturn(List.of(mockMessage));
        when(strategyService.chooseMessage(anyList(), eq(testGame))).thenReturn(mockMessage);
        when(mugloarService.solveMessage("test123", "ad1")).thenReturn(new MessageSolveResponse(false, 2, 100.0, 0, 0
                , 0, "failed"));

        List<ProcessedMessage> pmList = new ArrayList<>();
        boolean result = gameFacade.performMessageSolving(testGame, pmList);

        assertFalse(result);
        assertEquals(1, pmList.size());
        verify(strategyService, times(1)).recordFailure("ad1");
    }

    @Test
    void performMessageSolving_MugloarException() {
        when(mugloarService.getMessages("test123")).thenThrow(new MugloarException("Error"));
        List<ProcessedMessage> pmList = new ArrayList<>();

        boolean result = gameFacade.performMessageSolving(testGame, pmList);

        assertFalse(result);
        assertTrue(pmList.isEmpty());
    }

    @Test
    void performShopPhase_NoItemsAvailable() {
        when(mugloarService.getShopItems("test123")).thenReturn(null);

        assertDoesNotThrow(() -> gameFacade.performShopPhase(testGame));
        verifyNoInteractions(strategyService); // Strategy won't be called
    }

    @Test
    void performShopPhase_WithItems() {
        ShopItem shopItem = new ShopItem("hpot", "Healing Potion", 50.0);
        when(mugloarService.getShopItems("test123")).thenReturn(List.of(shopItem));
        when(strategyService.decideItemsToBuy(any(Game.class), anyList())).thenReturn(List.of(shopItem));

        ShopPurchaseResponse purchaseResp = new ShopPurchaseResponse("success", 90.0, 3, 1, 1, "");
        when(mugloarService.buyItem("test123", "hpot")).thenReturn(purchaseResp);

        assertDoesNotThrow(() -> gameFacade.performShopPhase(testGame));
    }

    @Test
    void finalizeGame_GameIsNull() {
        gameFacade.finalizeGame(null, new ArrayList<>());
        verifyNoInteractions(gameResultService);
    }

    @Test
    void finalizeGame_GameResultExists() {
        when(gameResultService.findByGameId("test123")).thenReturn(new GameResult());
        gameFacade.finalizeGame(testGame, new ArrayList<>());

        verify(gameResultService, never()).save(any());
    }

    @Test
    void finalizeGame_NoExistingResult_ScoreAbove1000() {
        testGame.setScore(1200);
        when(gameResultService.findByGameId("test123")).thenReturn(null);

        gameFacade.finalizeGame(testGame, new ArrayList<>());
        verify(gameResultService).save(any(GameResult.class));
    }

    @Test
    void finalizeGame_NoExistingResult_ScoreBelow1000() {
        testGame.setScore(500);
        when(gameResultService.findByGameId("test123")).thenReturn(null);

        gameFacade.finalizeGame(testGame, new ArrayList<>());
        verify(gameResultService).save(any(GameResult.class));
    }

    @Test
    void finalizeGame_DuplicateGameResultException() {
        when(gameResultService.findByGameId("test123")).thenReturn(null);
        doThrow(new DuplicateGameResultException("Duplicate", new Throwable())).when(gameResultService).save(any());

        assertDoesNotThrow(() -> gameFacade.finalizeGame(testGame, new ArrayList<>()));
    }

    @Test
    void finalizeGame_UnexpectedErrorWhileSaving() {
        when(gameResultService.findByGameId("test123")).thenReturn(null);
        doThrow(new RuntimeException("DB error")).when(gameResultService).save(any());

        assertDoesNotThrow(() -> gameFacade.finalizeGame(testGame, new ArrayList<>()));
    }

    @Test
    void terminateGame_GameNull() {
        assertDoesNotThrow(() -> gameFacade.terminateGame(null, Collections.emptyList(), "Reason"));

        verify(messagingTemplate, atLeastOnce()).convertAndSend(contains("/topic/game-status/Unknown"),
                Optional.ofNullable(any()));
    }

    @Test
    void terminateGame_WithReason() {
        assertDoesNotThrow(() -> gameFacade.terminateGame(testGame, new ArrayList<>(), "Termination reason"));
        verify(messagingTemplate, atLeastOnce()).convertAndSend(contains("/topic/game-status/test123"),
                Optional.ofNullable(any()));
    }

    @Test
    void updateGameStateFromSolveResponse_NullResponse() {
        gameFacade.updateGameStateFromSolveResponse(testGame, null);
        assertEquals(0, testGame.getScore()); // No update
    }

    @Test
    void updateGameStateFromSolveResponse_Success() {
        MessageSolveResponse resp = new MessageSolveResponse(true, 2, 80.0, 300, 0, 0, "success");
        gameFacade.updateGameStateFromSolveResponse(testGame, resp);

        assertEquals(300, testGame.getScore());
        assertEquals(2, testGame.getLives());
        assertEquals(80.0, testGame.getGold());
    }
}
package com.bigbank.mugloarserver.facades.integration;

import com.bigbank.mugloarserver.facades.GameFacade;
import com.bigbank.mugloarserver.models.*;
import com.bigbank.mugloarserver.services.GameResultService;
import com.bigbank.mugloarserver.services.InventoryService;
import com.bigbank.mugloarserver.services.MugloarService;
import com.bigbank.mugloarserver.services.StrategyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Integration tests for GameFacade
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
@Transactional
@SpringBootTest
@Import(GameFacadeIntegrationTest.TestConfig.class)
public class GameFacadeIntegrationTest {
    @Autowired
    private GameFacade gameFacade;
    @Autowired
    private GameResultService gameResultService;
    @Autowired
    private MugloarService mugloarService;
    @Autowired
    private StrategyService strategyService;
    @Autowired
    private InventoryService inventoryService;

    @BeforeEach
    void setupMocks() {
        Game mockGame = new Game();
        mockGame.setGameId("gameFacadeTest");
        mockGame.setLives(3);
        mockGame.setGold(100.0);
        mockGame.setLevel(1);

        when(mugloarService.startGame()).thenReturn(mockGame);

        when(mugloarService.investigate("gameFacadeTest")).thenAnswer(inv -> {
            if (mockGame.getLives() > 1) {
                mockGame.setLives(mockGame.getLives() - 1);
            }
            return new Investigation();
        });

        when(mugloarService.getMessages("gameFacadeTest"))
                .thenReturn(List.of(new Message("ad1", "msg1", "10", 3, null, "")))
                .thenReturn(List.of(new Message("ad2", "msg2", "20", 3, null, "")))
                .thenReturn(Collections.emptyList());

        when(strategyService.chooseMessage(anyList(), eq(mockGame)))
                .thenReturn(new Message("ad1", "msg1", "10", 3, null, ""))
                .thenReturn(new Message("ad2", "msg2", "20", 3, null, ""))
                .thenReturn(null);

        when(mugloarService.solveMessage(eq("gameFacadeTest"), anyString()))
                .thenReturn(new MessageSolveResponse(true, mockGame.getLives(), 120.0, 50, 200, 1, "solved"))
                .thenReturn(new MessageSolveResponse(true, mockGame.getLives(), 120.0, 100, 200, 2, "solved2"));

        when(mugloarService.getShopItems("gameFacadeTest"))
                .thenReturn(List.of(new ShopItem("hpot", "Healing Potion", 50.0)));

        when(strategyService.decideItemsToBuy(eq(mockGame), anyList()))
                .thenReturn(List.of(new ShopItem("hpot", "Healing Potion", 50.0)));

        when(mugloarService.buyItem(eq("gameFacadeTest"), eq("hpot")))
                .thenReturn(new ShopPurchaseResponse("success", 70.0, mockGame.getLives(), 1, 3));

        when(inventoryService.hasItem(anyString(), any(ShopItem.class))).thenReturn(false);
        doNothing().when(inventoryService).addItem(anyString(), any(ShopItem.class));
    }

    @Test
    void integration_FinalizeGame() {
        Game g = gameFacade.initializeGame();
        assertNotNull(g);
        List<ProcessedMessage> msgs = new ArrayList<>();
        msgs.add(new ProcessedMessage("adId", "Msg1", 0, 100, true, null));
        msgs.add(new ProcessedMessage("adId2", "Msg2", 1, 200, false, "fail reason"));
        gameFacade.finalizeGame(g, msgs);
        GameResult result = gameResultService.findByGameId("gameFacadeTest");
        assertNotNull(result);
        assertEquals(2, result.getProcessedMessages().size());
    }

    @Test
    void integration_TerminateGame() {
        Game g = gameFacade.initializeGame();
        assertNotNull(g);
        List<ProcessedMessage> msgs = new ArrayList<>();
        msgs.add(new ProcessedMessage("adTerm", "TermMsg", 0, 50, true, "done"));
        gameFacade.terminateGame(g, msgs, "force termination");
        GameResult result = gameResultService.findByGameId("gameFacadeTest");
        assertNotNull(result);
        assertEquals("gameFacadeTest", result.getGameId());
        assertFalse(result.getProcessedMessages().isEmpty());
    }

    @Test
    void integration_PerformInvestigation() {
        Game g = gameFacade.initializeGame();
        boolean ok = gameFacade.performInvestigation(g);
        assertTrue(ok);
    }

    @Test
    void integration_PerformMessageSolving() {
        Game g = gameFacade.initializeGame();
        List<ProcessedMessage> pm = new ArrayList<>();
        boolean solved = gameFacade.performMessageSolving(g, pm);
        assertTrue(solved);
        assertFalse(pm.isEmpty());
    }

    @Test
    void integration_PerformShopPhase() {
        Game g = gameFacade.initializeGame();
        gameFacade.performShopPhase(g);
        GameResult r = gameResultService.findByGameId("gameFacadeTest");
        assertNull(r);
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public SimpMessagingTemplate myMessagingTemplate() {
            return mock(SimpMessagingTemplate.class);
        }

        @Bean
        public MugloarService mugloarService() {
            return mock(MugloarService.class, RETURNS_DEEP_STUBS);
        }

        @Bean
        public StrategyService strategyService() {
            return mock(StrategyService.class);
        }

        @Bean
        public InventoryService inventoryService() {
            return mock(InventoryService.class);
        }
    }
}
package com.bigbank.mugloarserver.facades.integration;

import com.bigbank.mugloarserver.facades.GameFacade;
import com.bigbank.mugloarserver.models.Game;
import com.bigbank.mugloarserver.models.GameResult;
import com.bigbank.mugloarserver.models.ProcessedMessage;
import com.bigbank.mugloarserver.services.GameResultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for GameFacade
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
@SpringBootTest
@Transactional
public class GameFacadeIntegrationTest {
    @Autowired
    private GameFacade gameFacade;

    @Autowired
    private GameResultService gameResultService;

    @Test
    void integration_StartAndPlayGame() {
        Game initializedGame = gameFacade.initializeGame();
        assertNotNull(initializedGame);

        gameFacade.playGame(initializedGame);
        GameResult result = gameResultService.findByGameId(initializedGame.getGameId());

        assertNotNull(result);
        assertNotNull(result.getProcessedMessages());
        assertFalse(result.getProcessedMessages().isEmpty());
    }

    @Test
    void integration_FinalizeGame() {
        Game game = gameFacade.initializeGame();
        assertNotNull(game);

        List<ProcessedMessage> processedMessages = new ArrayList<>();
        processedMessages.add(new ProcessedMessage("adId", "Some Message", 0, 100, true, null));
        processedMessages.add(new ProcessedMessage("adId2", "Another Message", 1, 200, false, "failure reason"));

        gameFacade.finalizeGame(game, processedMessages);
        GameResult result = gameResultService.findByGameId(game.getGameId());

        assertNotNull(result);
        assertEquals(game.getGameId(), result.getGameId());
        assertEquals(2, result.getProcessedMessages().size());
    }
}

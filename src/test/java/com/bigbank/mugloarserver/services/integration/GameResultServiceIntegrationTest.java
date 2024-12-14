package com.bigbank.mugloarserver.services.integration;

import com.bigbank.mugloarserver.exceptions.DuplicateGameResultException;
import com.bigbank.mugloarserver.models.GameResult;
import com.bigbank.mugloarserver.services.GameResultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for GameResultService
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
@SpringBootTest
public class GameResultServiceIntegrationTest {
    @Autowired
    private GameResultService gameResultService;

    @Test
    void saveAndFindByGameId_Integration() throws DuplicateGameResultException {
        GameResult gr = new GameResult();
        gr.setGameId("intId");
        gameResultService.save(gr);
        GameResult found = gameResultService.findByGameId("intId");
        assertNotNull(found);
    }


    @Test
    void save_DuplicateGameResult_Integration() throws DuplicateGameResultException {
        GameResult gr = new GameResult();
        gr.setGameId("dupId");
        gameResultService.save(gr);

        GameResult gr2 = new GameResult();
        gr2.setGameId("dupId");

        assertThrows(DuplicateGameResultException.class, () -> gameResultService.save(gr2));
    }

    @Test
    void getAllGameResults_Integration() {
        var page = gameResultService.getAllGameResults(PageRequest.of(0, 10));
        assertNotNull(page);
    }
}

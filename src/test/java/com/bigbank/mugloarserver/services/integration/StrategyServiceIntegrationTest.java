package com.bigbank.mugloarserver.services.integration;

import com.bigbank.mugloarserver.models.Game;
import com.bigbank.mugloarserver.models.Investigation;
import com.bigbank.mugloarserver.models.Message;
import com.bigbank.mugloarserver.models.ShopItem;
import com.bigbank.mugloarserver.services.StrategyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for StrategyService
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
@SpringBootTest
public class StrategyServiceIntegrationTest {
    @Autowired
    private StrategyService strategyService;

    @Test
    void processInvestigation_Integration() {
        Investigation inv = new Investigation();
        inv.setPeople(10);
        inv.setState(5);
        inv.setUnderworld(15);
        strategyService.processInvestigation(inv);
    }

    @Test
    void chooseMessage_Integration() {
        Message m1 = new Message("m1", "Message1", "100", 5, null, "", Collections.emptyList(), "");
        Message m2 = new Message("m2", "Message2", "50", 2, null, "", Collections.emptyList(), "");
        Game game = new Game();
        game.setWingStrength(3);
        Message chosen = strategyService.chooseMessage(List.of(m1, m2), game);
        assertNotNull(chosen);
    }

    @Test
    void decideItemsToBuy_Integration() {
        Game game = new Game();
        game.setGold(200);
        ShopItem s1 = new ShopItem("hpot", "Healing Potion", 50.0);
        ShopItem s2 = new ShopItem("wingpot", "Wings", 100.0);
        ShopItem s3 = new ShopItem("other", "Other", 60.0);
        List<ShopItem> decided = strategyService.decideItemsToBuy(game, List.of(s1, s2, s3));
        assertTrue(decided.size() >= 2);
    }

    @Test
    void markMessageAsSolved_Integration() {
        strategyService.markMessageAsSolved("mid");
    }

    @Test
    void recordFailure_Integration() {
        strategyService.recordFailure("msgId");
        strategyService.recordFailure("msgId");
    }
}

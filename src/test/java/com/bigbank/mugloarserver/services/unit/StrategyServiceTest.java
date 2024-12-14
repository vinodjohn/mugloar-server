package com.bigbank.mugloarserver.services.unit;

import com.bigbank.mugloarserver.models.Game;
import com.bigbank.mugloarserver.models.Investigation;
import com.bigbank.mugloarserver.models.Message;
import com.bigbank.mugloarserver.models.ShopItem;
import com.bigbank.mugloarserver.services.implementations.StrategyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StrategyService
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
public class StrategyServiceTest {
    @InjectMocks
    private StrategyServiceImpl strategyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processInvestigation_ZeroTotals() {
        Investigation inv = new Investigation();
        strategyService.processInvestigation(inv);
        assertEquals(1.0, getPrivateField("peopleMultiplier"));
        assertEquals(1.0, getPrivateField("stateMultiplier"));
        assertEquals(1.0, getPrivateField("underworldMultiplier"));
    }

    @Test
    void processInvestigation_NonZeroTotals() {
        Investigation inv = new Investigation();
        inv.setPeople(20);
        inv.setState(10);
        inv.setUnderworld(20);
        strategyService.processInvestigation(inv);
        Object pm = getPrivateField("peopleMultiplier");
        Object sm = getPrivateField("stateMultiplier");
        Object um = getPrivateField("underworldMultiplier");
        assertNotNull(pm);
        assertNotNull(sm);
        assertNotNull(um);
        assertInstanceOf(Double.class, pm);
        assertInstanceOf(Double.class, sm);
        assertInstanceOf(Double.class, um);
        assertTrue((Double) pm > 0);
        assertTrue((Double) sm > 0);
        assertTrue((Double) um > 0);
    }

    @Test
    void chooseMessage_NoMessages() {
        Message result = strategyService.chooseMessage(null, new Game());
        assertNull(result);
    }

    @Test
    void chooseMessage_EmptyMessages() {
        Message result = strategyService.chooseMessage(Collections.emptyList(), new Game());
        assertNull(result);
    }

    @Test
    void chooseMessage_AllSolved() {
        Message msg1 = new Message("ad1", "msg", "100", 5, null, "", Collections.emptyList(), "");
        strategyService.markMessageAsSolved("ad1");
        Message result = strategyService.chooseMessage(List.of(msg1), new Game());
        assertNull(result);
    }

    @Test
    void chooseMessage_ReturnBestMessage() {
        Message msg1 = new Message("ad1", "msg1", "50", 5, null, "", Collections.emptyList(), "");
        msg1.setCategory("combat");
        Message msg2 = new Message("ad2", "msg2", "100", 2, null, "", Collections.emptyList(), "");
        msg2.setCategory("negotiation");
        Game game = new Game();
        game.setWingStrength(5);
        game.setScaleThickness(5);
        game.setFireBreath(5);
        game.setCunning(5);
        game.setClawSharpness(5);
        Message chosen = strategyService.chooseMessage(List.of(msg1, msg2), game);
        assertNotNull(chosen);
    }

    @Test
    void decideItemsToBuy_NoItems() {
        List<ShopItem> result = strategyService.decideItemsToBuy(new Game(), null);
        assertTrue(result.isEmpty());
    }

    @Test
    void decideItemsToBuy_EmptyItems() {
        List<ShopItem> result = strategyService.decideItemsToBuy(new Game(), Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    void decideItemsToBuy_RequiredItemsAndGold() {
        Game game = new Game();
        game.setGold(300);
        ShopItem hpot = new ShopItem("hpot", "Healing Potion", 50.0);
        ShopItem wingpot = new ShopItem("wingpot", "Potion of Awesome Wings", 100.0);
        ShopItem mtrix = new ShopItem("mtrix", "Matrix", 120.0);
        ShopItem other = new ShopItem("other", "OtherItem", 70.0);
        List<ShopItem> selected = strategyService.decideItemsToBuy(game, List.of(hpot, wingpot, mtrix, other));
        assertTrue(selected.contains(hpot));
        assertTrue(selected.contains(wingpot));
        assertTrue(selected.contains(mtrix));
    }

    @Test
    void decideItemsToBuy_AdditionalItemsAfterRequired() {
        Game game = new Game();
        game.setGold(500);
        ShopItem hpot = new ShopItem("hpot", "Healing Potion", 50.0);
        ShopItem wingpot = new ShopItem("wingpot", "Wings", 100.0);
        ShopItem other = new ShopItem("other", "OtherItem", 70.0);
        ShopItem other2 = new ShopItem("other2", "OtherItem2", 200.0);
        List<ShopItem> result = strategyService.decideItemsToBuy(game, List.of(hpot, wingpot, other, other2));
        assertTrue(result.contains(hpot));
        assertTrue(result.contains(wingpot));
        assertTrue(result.contains(other));
        assertTrue(result.contains(other2));
    }

    @Test
    void markMessageAsSolved_NullAdId() {
        strategyService.markMessageAsSolved(null);
    }

    @Test
    void markMessageAsSolved_ValidId() {
        strategyService.markMessageAsSolved("ad1");
    }

    @Test
    void recordFailure_Increment() {
        strategyService.recordFailure("adX");
        strategyService.recordFailure("adX");
        assertEquals(2, getPrivateMapValue());
    }

    // PRIVATE METHODS //
    private Object getPrivateField(String fieldName) {
        try {
            var field = StrategyServiceImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(strategyService);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private int getPrivateMapValue() {
        try {
            var field = StrategyServiceImpl.class.getDeclaredField("failureCounts");
            field.setAccessible(true);
            Map<String, Integer> map = (Map<String, Integer>) field.get(strategyService);
            return map.getOrDefault("adX", 0);
        } catch (Exception e) {
            return -1;
        }
    }
}

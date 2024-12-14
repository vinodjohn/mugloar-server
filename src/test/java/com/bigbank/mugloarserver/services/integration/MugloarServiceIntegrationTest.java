package com.bigbank.mugloarserver.services.integration;

import com.bigbank.mugloarserver.services.MugloarService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests for Mugloar Service
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
@SpringBootTest
public class MugloarServiceIntegrationTest {
    @Autowired
    private MugloarService mugloarService;

    @Test
    void startGame_Integration() {
        assertThrows(Exception.class, () -> mugloarService.startGame());
    }

    @Test
    void investigate_Integration() {
        assertThrows(Exception.class, () -> mugloarService.investigate("someId"));
    }

    @Test
    void getMessages_Integration() {
        assertThrows(Exception.class, () -> mugloarService.getMessages("someId"));
    }

    @Test
    void solveMessage_Integration() {
        assertThrows(Exception.class, () -> mugloarService.solveMessage("someId", "adId"));
    }

    @Test
    void getShopItems_Integration() {
        assertThrows(Exception.class, () -> mugloarService.getShopItems("someId"));
    }

    @Test
    void buyItem_Integration() {
        assertThrows(Exception.class, () -> mugloarService.buyItem("someId", "itemId"));
    }
}

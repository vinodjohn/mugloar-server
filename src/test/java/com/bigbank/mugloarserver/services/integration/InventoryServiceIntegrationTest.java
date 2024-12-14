package com.bigbank.mugloarserver.services.integration;

import com.bigbank.mugloarserver.services.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for InventoryService
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
@SpringBootTest
public class InventoryServiceIntegrationTest {
    @Autowired
    private InventoryService inventoryService;

    @Test
    void integration_AddAndCheckItem() {
        assertFalse(inventoryService.hasItem("gameInt", "shield"));
        inventoryService.addItem("gameInt", "shield");
        assertTrue(inventoryService.hasItem("gameInt", "shield"));
    }

    @Test
    void integration_CheckItemCaseInsensitivity() {
        inventoryService.addItem("gameInt", "SWORD");
        assertTrue(inventoryService.hasItem("gameInt", "sword"));
    }

    @Test
    void integration_SeparateGames() {
        inventoryService.addItem("gameX", "potion");
        assertTrue(inventoryService.hasItem("gameX", "potion"));
        assertFalse(inventoryService.hasItem("gameY", "potion"));
    }
}

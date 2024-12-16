package com.bigbank.mugloarserver.services.integration;

import com.bigbank.mugloarserver.models.ShopItem;
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
        assertTrue(inventoryService.hasItem("gameInt", new ShopItem("2", "itemY", 120)));
        inventoryService.addItem("gameInt", new ShopItem("2", "itemY", 120));
        assertTrue(inventoryService.hasItem("gameInt", new ShopItem("2", "itemY", 120)));
    }

    @Test
    void integration_CheckItemCaseInsensitivity() {
        inventoryService.addItem("gameInt", new ShopItem("2", "itemY", 120));
        assertTrue(inventoryService.hasItem("gameInt", new ShopItem("2", "itemY", 120)));
    }

    @Test
    void integration_SeparateGames() {
        inventoryService.addItem("gameX", new ShopItem("2", "itemY", 120));
        assertTrue(inventoryService.hasItem("gameX", new ShopItem("2", "itemY", 120)));
        assertFalse(inventoryService.hasItem("gameY", new ShopItem("2", "itemY", 120)));
    }
}

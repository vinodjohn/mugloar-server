package com.bigbank.mugloarserver.services.unit;

import com.bigbank.mugloarserver.models.ShopItem;
import com.bigbank.mugloarserver.services.implementations.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for InventoryService
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
public class InventoryServiceTest {
    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void hasItem_EmptyStore() {
        assertFalse(inventoryService.hasItem("game1", new ShopItem("1", "itemX", 100)));
    }

    @Test
    void hasItem_ItemExists() {
        inventoryService.addItem("game1", new ShopItem("1", "itemX", 100));
        assertTrue(inventoryService.hasItem("game1", new ShopItem("1", "itemX", 100)));
    }

    @Test
    void hasItem_DifferentCase() {
        inventoryService.addItem("game1", new ShopItem("1", "itemX", 100));
        assertTrue(inventoryService.hasItem("game1", new ShopItem("1", "itemX", 100)));
    }

    @Test
    void hasItem_DifferentGame() {
        inventoryService.addItem("game1", new ShopItem("1", "itemX", 100));
        assertFalse(inventoryService.hasItem("game2", new ShopItem("1", "itemX", 100)));
    }

    @Test
    void addItem_NewGame() {
        assertFalse(inventoryService.hasItem("gameNew", new ShopItem("1", "itemX", 100)));
        inventoryService.addItem("gameNew", new ShopItem("1", "itemX", 100));
        assertTrue(inventoryService.hasItem("gameNew", new ShopItem("1", "itemX", 100)));
    }

    @Test
    void addItem_ExistingGameMultipleItems() {
        inventoryService.addItem("game1", new ShopItem("1", "itemX", 100));
        inventoryService.addItem("game1", new ShopItem("2", "itemY", 120));
        assertTrue(inventoryService.hasItem("game1", new ShopItem("1", "itemX", 100)));
        assertTrue(inventoryService.hasItem("game1", new ShopItem("2", "itemY", 120)));
    }
}

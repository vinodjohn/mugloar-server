package com.bigbank.mugloarserver.services.unit;

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
        assertFalse(inventoryService.hasItem("game1", "itemX"));
    }

    @Test
    void hasItem_ItemExists() {
        inventoryService.addItem("game1", "itemX");
        assertTrue(inventoryService.hasItem("game1", "itemX"));
    }

    @Test
    void hasItem_DifferentCase() {
        inventoryService.addItem("game1", "ITEMX");
        assertTrue(inventoryService.hasItem("game1", "itemx"));
    }

    @Test
    void hasItem_DifferentGame() {
        inventoryService.addItem("game1", "itemA");
        assertFalse(inventoryService.hasItem("game2", "itemA"));
    }

    @Test
    void addItem_NewGame() {
        assertFalse(inventoryService.hasItem("gameNew", "itemA"));
        inventoryService.addItem("gameNew", "itemA");
        assertTrue(inventoryService.hasItem("gameNew", "itemA"));
    }

    @Test
    void addItem_ExistingGameMultipleItems() {
        inventoryService.addItem("game1", "itemA");
        inventoryService.addItem("game1", "itemB");
        assertTrue(inventoryService.hasItem("game1", "itemA"));
        assertTrue(inventoryService.hasItem("game1", "itemB"));
    }
}

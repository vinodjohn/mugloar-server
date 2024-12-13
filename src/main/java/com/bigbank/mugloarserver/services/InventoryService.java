package com.bigbank.mugloarserver.services;

/**
 * Service interface for managing owned shop items.
 *
 * @author vinodjohn
 * @created 10.12.2024
 */
public interface InventoryService {
    /**
     * Checks if the player owns a specific item.
     *
     * @param gameId The ID of the current game.
     * @param itemId The ID of the item.
     * @return true if the item is owned, false otherwise.
     */
    boolean hasItem(String gameId, String itemId);

    /**
     * To save a new item
     *
     * @param gameId The ID of the current game.
     * @param itemId The ID of the item.
     */
    void addItem(String gameId, String itemId);
}

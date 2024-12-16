package com.bigbank.mugloarserver.services;

import com.bigbank.mugloarserver.models.ShopItem;

import java.util.List;

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
     * @param item   The ShopItem.
     * @return true if the item is owned, false otherwise.
     */
    boolean hasItem(String gameId, ShopItem item);

    /**
     * To save a new item
     *
     * @param gameId The ID of the current game.
     * @param item   The ShopItem.
     */
    void addItem(String gameId, ShopItem item);

    /**
     * To get all items by GameID
     *
     * @param gameId The ID of the current game.
     * @return List of ShopItem
     */
    List<ShopItem> getAllByGameId(String gameId);
}

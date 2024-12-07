package com.bigbank.mugloarserver.services;

import com.bigbank.mugloarserver.models.*;

/**
 * Service interface for interacting with the Mugloar API, starting games, investigating, retrieving messages,
 * solving tasks, and interacting with the shop.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
public interface MugloarService {
    /**
     * Starts a new game using the Mugloar API.
     *
     * @return the started Game state
     */
    Game startGame();

    /**
     * Runs an investigation about the player's reputation.
     *
     * @param gameId the unique ID of the current game
     * @return the Investigation result
     */
    Investigation investigate(String gameId);

    /**
     * Retrieves all messages (tasks) available for the given game.
     *
     * @param gameId the unique ID of the game
     * @return an array of Messages available
     */
    Message[] getMessages(String gameId);

    /**
     * Attempts to solve a specific message (task).
     *
     * @param gameId the unique ID of the game
     * @param adId   the ID of the message to solve
     * @return the result of the solve attempt
     */
    MessageSolveResponse solveMessage(String gameId, String adId);

    /**
     * Retrieves a list of items available in the shop for the current game.
     *
     * @param gameId the unique ID of the game
     * @return an array of ShopItems available for purchase
     */
    ShopItem[] getShopItems(String gameId);

    /**
     * Attempts to buy a specific item from the shop.
     *
     * @param gameId the unique ID of the game
     * @param itemId the ID of the item to purchase
     * @return the result of the purchase attempt
     */
    ShopPurchaseResponse buyItem(String gameId, String itemId);
}

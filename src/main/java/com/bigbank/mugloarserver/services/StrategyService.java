package com.bigbank.mugloarserver.services;

import com.bigbank.mugloarserver.models.Game;
import com.bigbank.mugloarserver.models.Investigation;
import com.bigbank.mugloarserver.models.Message;
import com.bigbank.mugloarserver.models.ShopItem;

import java.util.List;

/**
 * Service interface for strategy logic, including choosing tasks, processing investigations, and deciding whether to
 * buy items.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
public interface StrategyService {
    /**
     * Processes the investigation results to adjust the game strategy.
     *
     * @param investigation The result of the investigation.
     */
    void processInvestigation(Investigation investigation);

    /**
     * Chooses the best message to solve based on the current strategy.
     *
     * @param messages The list of available messages.
     * @param game     The current game state.
     * @return The selected message to solve.
     */
    Message chooseMessage(List<Message> messages, Game game);

    /**
     * Decides which shop items to buy based on the current game state.
     *
     * @param game      The current game state.
     * @param shopItems The list of available shop items.
     * @return The list of items selected to purchase.
     */
    List<ShopItem> decideItemsToBuy(Game game, List<ShopItem> shopItems);

    /**
     * Marks a message as solved to avoid reprocessing.
     *
     * @param adId The identifier of the solved message.
     */
    void markMessageAsSolved(String adId);

    /**
     * Marks a message as failed.
     *
     * @param adId The identifier of the failed message.
     */
    void recordFailure(String adId);
}
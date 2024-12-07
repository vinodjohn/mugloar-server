package com.bigbank.mugloarserver.services;

import com.bigbank.mugloarserver.models.GameResult;

import java.util.List;

/**
 * Service interface for managing GameResult entities.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
public interface GameResultService {
    /**
     * Retrieves all recorded game results.
     *
     * @return a list of all GameResult entities
     */
    List<GameResult> findAll();

    /**
     * Saves a new game result to the repository.
     *
     * @param result the GameResult entity to save
     * @return the saved GameResult entity
     */
    GameResult save(GameResult result);
}

package com.bigbank.mugloarserver.services;

import com.bigbank.mugloarserver.exceptions.DuplicateGameResultException;
import com.bigbank.mugloarserver.models.GameResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing GameResult entities.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
public interface GameResultService {
    /**
     * Saves a GameResult to the repository.
     *
     * @param gameResult The GameResult object to save.
     * @throws DuplicateGameResultException if a GameResult with the same gameId already exists.
     */
    void save(GameResult gameResult) throws DuplicateGameResultException;

    /**
     * Retrieves a GameResult by its gameId.
     *
     * @param gameId The unique identifier of the game.
     * @return The corresponding GameResult, or null if not found.
     */
    GameResult findByGameId(String gameId);

    /**
     * Retrieves all GameResult entries.
     *
     * @return A page of all GameResults.
     */
    Page<GameResult> getAllGameResults(Pageable pageable);
}

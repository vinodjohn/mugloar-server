package com.bigbank.mugloarserver.repositories;

import com.bigbank.mugloarserver.models.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository to handle GameResult related data queries
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Repository
public interface GameResultRepository extends PagingAndSortingRepository<GameResult, UUID>, JpaRepository<GameResult,
        UUID> {
    /**
     * Finds a GameResult by its gameId.
     *
     * @param gameId The unique identifier of the game.
     * @return The corresponding GameResult, or null if not found.
     */
    GameResult findByGameId(String gameId);
}

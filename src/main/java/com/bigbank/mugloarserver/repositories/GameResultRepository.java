package com.bigbank.mugloarserver.repositories;

import com.bigbank.mugloarserver.models.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository to handle GameResult related data queries
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Repository
public interface GameResultRepository extends JpaRepository<GameResult, UUID> {
}

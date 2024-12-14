package com.bigbank.mugloarserver.services.implementations;

import com.bigbank.mugloarserver.exceptions.DuplicateGameResultException;
import com.bigbank.mugloarserver.models.GameResult;
import com.bigbank.mugloarserver.repositories.GameResultRepository;
import com.bigbank.mugloarserver.services.GameResultService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of GameResultService
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Service
public class GameResultServiceImpl implements GameResultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameResultServiceImpl.class);

    private final GameResultRepository gameResultRepository;

    public GameResultServiceImpl(GameResultRepository gameResultRepository) {
        this.gameResultRepository = gameResultRepository;
    }

    @Override
    @Transactional
    public void save(GameResult gameResult) throws DuplicateGameResultException {
        try {
            gameResultRepository.saveAndFlush(gameResult);
            LOGGER.info("GameResult saved successfully for GameID={}.", gameResult.getGameId());
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("Attempted to save duplicate GameResult for GameID={}.", gameResult.getGameId());
            throw new DuplicateGameResultException("GameResult with GameID '" + gameResult.getGameId() + "' already " +
                    "exists.", e);
        } catch (Exception e) {
            LOGGER.error("An error occurred while saving GameResult for GameID={}: {}", gameResult.getGameId(),
                    e.getMessage(), e);
            throw new RuntimeException("Failed to save GameResult.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public GameResult findByGameId(String gameId) {
        try {
            GameResult result = gameResultRepository.findByGameId(gameId);

            if (result != null) {
                LOGGER.info("Found GameResult for GameID={}.", gameId);
            } else {
                LOGGER.warn("No GameResult found for GameID={}.", gameId);
            }
            return result;
        } catch (Exception e) {
            LOGGER.error("An error occurred while retrieving GameResult for GameID={}: {}", gameId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve GameResult.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameResult> getAllGameResults(Pageable pageable) {
        try {
            Page<GameResult> results = gameResultRepository.findAll(pageable);
            LOGGER.info("Retrieved {} GameResult entries.", results.getTotalElements());
            return results;
        } catch (Exception e) {
            LOGGER.error("An error occurred while retrieving all GameResults: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve all GameResults.", e);
        }
    }
}

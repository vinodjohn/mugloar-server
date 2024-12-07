package com.bigbank.mugloarserver.services.implementations;

import com.bigbank.mugloarserver.models.GameResult;
import com.bigbank.mugloarserver.repositories.GameResultRepository;
import com.bigbank.mugloarserver.services.GameResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Implementation of GameResultService
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Service
public class GameResultServiceImpl implements GameResultService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameResultServiceImpl.class);

    @Autowired
    private GameResultRepository gameResultRepository;

    @Override
    public List<GameResult> findAll() {
        LOGGER.info("Retrieving all game results from the repository.");
        return gameResultRepository.findAll();
    }

    @Override
    public GameResult save(GameResult result) {
        LOGGER.info("Saving a new game result: GameID={}, Score={}", result.getGameId(), result.getFinalScore());
        return gameResultRepository.save(result);
    }
}

package com.bigbank.mugloarserver.facades;

import com.bigbank.mugloarserver.models.*;
import com.bigbank.mugloarserver.services.GameResultService;
import com.bigbank.mugloarserver.services.MugloarService;
import com.bigbank.mugloarserver.services.StrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * A facade that orchestrates the game flow: start a game, investigate, solve tasks and store the result.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Component
public class GameFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameFacade.class);

    @Autowired
    private MugloarService mugloarService;

    @Autowired
    private StrategyService strategyService;

    @Autowired
    private GameResultService gameResultService;

    public Game playGame() {
        Game game = mugloarService.startGame();
        LOGGER.info("Game started: {} | Lives={} | Gold={} | Score={}",
                game.getGameId(), game.getLives(), game.getGold(), game.getScore());

        while (game.getLives() > 0) {
            Investigation inv = mugloarService.investigate(game.getGameId());
            strategyService.processInvestigation(inv);

            Message[] messages = mugloarService.getMessages(game.getGameId());
            Message chosenMessage = strategyService.chooseMessage(messages);

            if (chosenMessage == null) {
                LOGGER.info("No suitable messages left. Stopping.");
                break;
            }

            MessageSolveResponse messageSolveResponse = mugloarService.solveMessage(game.getGameId(), chosenMessage.getAdId());

            LOGGER.info("Solved message {}: success={}, score={}, lives={}, gold={}, message='{}'",
                    chosenMessage.getAdId(), messageSolveResponse.isSuccess(), messageSolveResponse.getScore(),
                    messageSolveResponse.getLives(), messageSolveResponse.getGold(), messageSolveResponse.getMessage());

            game.setLives(messageSolveResponse.getLives());
            game.setScore(messageSolveResponse.getScore());
            game.setGold(messageSolveResponse.getGold());
        }

        boolean hasAchievedGoal = game.getScore() >= 1000;

        if (!hasAchievedGoal) {
            LOGGER.warn("Finished with score {} < 1000.", game.getScore());
        } else {
            LOGGER.info("Finished successfully with score {}!", game.getScore());
        }

        GameResult gr = new GameResult(null, game.getGameId(), game.getScore(), game.getLives(), hasAchievedGoal,
                LocalDateTime.now());

        gameResultService.save(gr);

        return game;
    }
}

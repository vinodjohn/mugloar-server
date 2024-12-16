package com.bigbank.mugloarserver.facades;

import com.bigbank.mugloarserver.exceptions.DuplicateGameResultException;
import com.bigbank.mugloarserver.exceptions.GameOverException;
import com.bigbank.mugloarserver.exceptions.MugloarException;
import com.bigbank.mugloarserver.models.*;
import com.bigbank.mugloarserver.services.GameResultService;
import com.bigbank.mugloarserver.services.InventoryService;
import com.bigbank.mugloarserver.services.MugloarService;
import com.bigbank.mugloarserver.services.StrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public Game initializeGame() {
        try {
            Game game = mugloarService.startGame();

            LOGGER.info("Game started: GameID={} | Lives={} | Gold={} | Level={} | Score={} | " +
                            "Turn={}| Dragon Level={}",
                    game.getGameId(), game.getLives(), game.getGold(), game.getLevel(), game.getScore(),
                    game.getTurn(), game.getDragonLevel());

            sendGameStateUpdate(game.getGameId(), "game_initialized", "Game initialized.");

            return game;
        } catch (GameOverException goe) {
            LOGGER.warn("Game Over detected during initialization: {}. Terminating the game.", goe.getMessage());
            terminateGame(new Game(), new ArrayList<>(), "Game Over detected during initialization.");
            throw goe;
        } catch (MugloarException me) {
            LOGGER.error("MugloarException during game initialization: {}. Continuing the game.", me.getCode());
            return null;
        } catch (Exception e) {
            LOGGER.error("Failed to start game: {}. Continuing the game.", e.getMessage(), e);
            return null;
        }
    }

    public void playGame(Game game) {
        if (game == null) {
            LOGGER.error("Game is null. Cannot proceed with game play.");
            return;
        }

        List<ProcessedMessage> processedMessages = new ArrayList<>();

        while (true) {
            try {
                LOGGER.debug("Starting a new game loop for GameID={}", game.getGameId());

                // Investigation Phase
                if (!performInvestigation(game)) {
                    LOGGER.warn("Investigation failed for GameID={}. Continuing the game.", game.getGameId());
                    sendGameStateUpdate(game.getGameId(), "investigation_failed", "Investigation failed.");
                } else {
                    sendGameStateUpdate(game.getGameId(), "investigation_completed", "Investigation phase completed.");
                }

                // Message Solving Phase
                boolean solvedAnyMessage = performMessageSolving(game, processedMessages);

                if (!solvedAnyMessage) {
                    LOGGER.info("No messages solved in this loop for GameID={}. Continuing the game.",
                            game.getGameId());
                    sendGameStateUpdate(game.getGameId(), "no_messages_solved", "No messages solved in this loop.");
                } else {
                    sendGameStateUpdate(game.getGameId(), "messages_solved", "Solved messages successfully.");
                }

                // Shop Phase: Attempt to purchase items before the next message solving
                performShopPhase(game);
                sendGameStateUpdate(game.getGameId(), "shop_phase_completed", "Completed Shop Phase.");

                sendGameStateUpdate(game.getGameId(), "game_loop_completed", "Completed a game loop.");
            } catch (GameOverException goe) {
                LOGGER.warn("Game Over detected: {}. Terminating the game.", goe.getMessage());

                sendGameStateUpdate(game.getGameId(), "game_over", "Game Over detected.");
                terminateGame(game, processedMessages, "Game Over detected from API.");

                break;
            } catch (MugloarException me) {
                LOGGER.error("MugloarException occurred: {}. Continuing the game.", me.getCode());
                sendGameStateUpdate(game.getGameId(), "game_error", "Encountered an error during game.");
            } catch (Exception e) {
                LOGGER.error("An unexpected error occurred: {}. Continuing the game.", e.getMessage(), e);
                sendGameStateUpdate(game.getGameId(), "unexpected_error", "Unexpected error occurred.");
            }
        }
    }

    public boolean performInvestigation(Game game) {
        try {
            Investigation investigation = mugloarService.investigate(game.getGameId());

            if (investigation == null) {
                LOGGER.warn("Investigation API returned null for GameID={}.", game.getGameId());
                return false;
            }

            strategyService.processInvestigation(investigation);

            LOGGER.info("Investigation phase completed for GameID={}.", game.getGameId());
            return true;
        } catch (GameOverException goe) {
            throw goe;
        } catch (MugloarException me) {
            LOGGER.error("MugloarException during investigation: {}. Continuing the game.", me.getCode());
            sendGameStateUpdate(game.getGameId(), "game_error", "Encountered an error during investigation.");
            return false;
        } catch (Exception e) {
            LOGGER.error("Failed to investigate for GameID={}: {}. Continuing the game.", game.getGameId(),
                    e.getMessage(), e);
            sendGameStateUpdate(game.getGameId(), "unexpected_error", "Error occurred during investigation.");
            return false;
        }
    }

    public boolean performMessageSolving(Game game, List<ProcessedMessage> processedMessages) {
        try {
            List<Message> messages = mugloarService.getMessages(game.getGameId());

            if (messages == null || messages.isEmpty()) {
                LOGGER.info("No messages available for GameID={}.", game.getGameId());
                sendGameStateUpdate(game.getGameId(), "no_messages_available", "No messages available to solve.");
                return false;
            }

            List<Message> allMessages = new ArrayList<>(messages);
            boolean anyMessageSolved = false;

            while (!allMessages.isEmpty()) {
                Message chosenMessage = strategyService.chooseMessage(allMessages, game);

                if (chosenMessage == null) {
                    LOGGER.info("No suitable messages left to solve for GameID={}.", game.getGameId());
                    sendGameStateUpdate(game.getGameId(), "no_suitable_messages", "No suitable messages left to solve" +
                            ".");
                    break;
                }

                sendGameStateUpdate(game.getGameId(), "now_solving_message", chosenMessage.getMessage());

                // Attempt to solve the message
                MessageSolveResponse solveResponse = mugloarService.solveMessage(game.getGameId(),
                        chosenMessage.getAdId());

                if (solveResponse != null && solveResponse.isSuccess()) {
                    LOGGER.info("Successfully solved message '{}'.", chosenMessage.getAdId());
                    updateGameStateFromSolveResponse(game, solveResponse);

                    ProcessedMessage processedMessage = new ProcessedMessage(
                            chosenMessage.getAdId(),
                            chosenMessage.getMessage(),
                            game.getTurn(),
                            chosenMessage.getIntReward(),
                            true,
                            solveResponse.getMessage()
                    );
                    processedMessages.add(processedMessage);

                    strategyService.markMessageAsSolved(chosenMessage.getAdId());
                    anyMessageSolved = true;
                    sendGameStateUpdate(game.getGameId(), "message_solved", chosenMessage.getMessage());

                } else {
                    LOGGER.warn("Failed to solve message '{}'. Lives left: {}", chosenMessage.getAdId(),
                            game.getLives());

                    ProcessedMessage processedMessage = new ProcessedMessage(
                            chosenMessage.getAdId(),
                            chosenMessage.getMessage(),
                            game.getTurn(),
                            chosenMessage.getIntReward(),
                            false,
                            solveResponse != null ? solveResponse.getMessage() : "API returned failure response."
                    );

                    processedMessages.add(processedMessage);
                    strategyService.recordFailure(chosenMessage.getAdId());
                    sendGameStateUpdate(game.getGameId(), "message_failed", chosenMessage.getMessage());
                }

                allMessages.remove(chosenMessage);
                performShopPhase(game);
            }

            return anyMessageSolved;
        } catch (GameOverException goe) {
            LOGGER.warn("Game Over detected during message solving: {}", goe.getMessage());
            sendGameStateUpdate(game.getGameId(), "game_over", "Game Over detected during message solving.");
            throw goe;
        } catch (MugloarException me) {
            LOGGER.error("MugloarException during message solving: {}. Continuing the game.", me.getCode());
            sendGameStateUpdate(game.getGameId(), "game_error", "Encountered an error during message solving.");
            return false;
        } catch (Exception e) {
            LOGGER.error("Error during message solving for GameID={}: {}. Continuing the game.", game.getGameId(),
                    e.getMessage(), e);
            sendGameStateUpdate(game.getGameId(), "unexpected_error", "Error occurred during message solving.");
            return false;
        }
    }

    public void performShopPhase(Game game) {
        try {
            List<ShopItem> shopItems = mugloarService.getShopItems(game.getGameId());

            if (shopItems == null || shopItems.isEmpty()) {
                LOGGER.info("No shop items available for GameID={}.", game.getGameId());
                sendGameStateUpdate(game.getGameId(), "no_shop_items", "No shop items available.");
                return;
            }

            List<ShopItem> itemsToBuy = strategyService.decideItemsToBuy(game, shopItems);

            for (ShopItem item : itemsToBuy) {
                try {
                    sendGameStateUpdate(game.getGameId(), "now_purchasing_item", item.getName());
                    ShopPurchaseResponse purchaseResponse = mugloarService.buyItem(game.getGameId(), item.getId());

                    if (purchaseResponse != null && purchaseResponse.isSuccess()) {
                        LOGGER.info("Bought item '{}'.", item.getName());

                        updateGameStateFromPurchaseResponse(game, purchaseResponse, item.getName());
                        inventoryService.addItem(game.getGameId(), item);
                        sendGameStateUpdate(game.getGameId(), "item_purchased", item.getName());
                    } else {
                        LOGGER.warn("Failed to buy item '{}'.", item.getName());
                        sendGameStateUpdate(game.getGameId(), "item_purchase_failed", item.getName());
                    }
                } catch (GameOverException goe) {
                    LOGGER.warn("Game Over detected during item purchase: {}", goe.getMessage());
                    sendGameStateUpdate(game.getGameId(), "game_over", "Game Over detected during item purchase.");
                    throw goe;
                } catch (MugloarException me) {
                    LOGGER.error("MugloarException during item purchase: {}. Continuing the game.", me.getCode());
                    sendGameStateUpdate(game.getGameId(), "shop_error", "Encountered an error during item purchase.");
                } catch (Exception e) {
                    LOGGER.error("Error buying item '{}': {}. Continuing the game.", item.getName(), e.getMessage(), e);
                    sendGameStateUpdate(game.getGameId(), "shop_unexpected_error", item.getName());
                }
            }

            if (itemsToBuy.isEmpty()) {
                LOGGER.info("No items decided to buy for GameID={}.", game.getGameId());
                sendGameStateUpdate(game.getGameId(), "no_items_to_buy", "No items decided to purchase.");
            }
        } catch (GameOverException goe) {
            LOGGER.warn("Game Over detected during shopping phase: {}", goe.getMessage());
            sendGameStateUpdate(game.getGameId(), "game_over", "Game Over detected during shopping phase.");
            throw goe;
        } catch (MugloarException me) {
            LOGGER.error("MugloarException during shop phase: {}. Continuing the game.", me.getCode());
            sendGameStateUpdate(game.getGameId(), "shop_error", "Encountered an error during shopping phase.");
        } catch (Exception e) {
            LOGGER.error("Error during shop phase for GameID={}: {}. Continuing the game.", game.getGameId(),
                    e.getMessage(), e);
            sendGameStateUpdate(game.getGameId(), "shop_unexpected_error", "Error occurred during shopping phase.");
        }
    }

    public void finalizeGame(Game game, List<ProcessedMessage> processedMessages) {
        if (game == null) {
            LOGGER.warn("Game is null. Cannot finalize a non-existent game.");
            sendGameStateUpdate("Unknown", "unknown_finalization", "Game is null. Cannot finalize a non-existent game" +
                    ".");
            return;
        }

        GameResult existingResult = gameResultService.findByGameId(game.getGameId());

        if (existingResult != null) {
            LOGGER.warn("GameResult for GameID={} already exists. Skipping save.", game.getGameId());
            sendGameStateUpdate(game.getGameId(), "game_result_exists", "Game result already exists. Skipping save.");
            return;
        }

        boolean achievedGoal = game.getScore() >= 1000;

        if (achievedGoal) {
            LOGGER.info("Congratulations! Achieved the minimum score of 1000 with Score={} for GameID={}.",
                    game.getScore(), game.getGameId());
            sendGameStateUpdate(game.getGameId(), "game_completed", "Congratulations! Achieved the minimum score of " +
                    "1000.");
        } else {
            LOGGER.warn("Game over for GameID={}. Final Score={} did not achieve the minimum goal.",
                    game.getGameId(), game.getScore());
            sendGameStateUpdate(game.getGameId(), "game_over", "Game over. Final score did not achieve the minimum " +
                    "goal.");
        }

        List<ShopItem> purchasedItems = inventoryService.getAllByGameId(game.getGameId());

        GameResult gameResult = new GameResult(
                null,
                game.getGameId(),
                game.getScore(),
                game.getHighScore(),
                game.getLives(),
                game.getGold(),
                game.getLevel(),
                game.getTurn(),
                achievedGoal,
                LocalDateTime.now(),
                processedMessages,
                purchasedItems
        );

        try {
            gameResultService.save(gameResult);
            LOGGER.info("GameResult saved: {}", gameResult);
            sendGameStateUpdate(game.getGameId(), "game_result_saved", "Game result saved successfully.");
        } catch (DuplicateGameResultException e) {
            LOGGER.error("Duplicate GameResult detected for GameID={}: {}", game.getGameId(), e.getMessage());
            sendGameStateUpdate(game.getGameId(), "duplicate_game_result", "Duplicate game result detected.");
        } catch (Exception e) {
            LOGGER.error("Failed to save GameResult for GameID={}: {}", game.getGameId(), e.getMessage(), e);
            sendGameStateUpdate(game.getGameId(), "game_result_save_failed", "Failed to save game result.");
        }
    }

    public void terminateGame(Game game, List<ProcessedMessage> processedMessages, String terminationReason) {
        if (game == null) {
            LOGGER.warn("Game is null. Using a default game state for termination.");
            sendGameStateUpdate("Unknown", "unknown_termination", "Terminating a non-existent game.");
            game = new Game();
        }

        LOGGER.info("Terminating game for GameID={} due to: {}", game.getGameId(), terminationReason);
        sendGameStateUpdate(game.getGameId(), "game_terminated", terminationReason);
        finalizeGame(game, processedMessages);
    }

    public void updateGameStateFromSolveResponse(Game game, MessageSolveResponse solveResponse) {
        if (solveResponse == null) {
            LOGGER.warn("Solve response is null. No game state updated.");
            sendGameStateUpdate(game.getGameId(), "solve_response_null", "Solve response is null.");
            return;
        }

        game.setScore(solveResponse.getScore());
        game.setLives(solveResponse.getLives());
        game.setGold(solveResponse.getGold());
        game.setTurn(solveResponse.getTurn());

        LOGGER.debug("Game state updated after solving message. Current state: {}", game);
        sendGameStateUpdate(game.getGameId(), "solve_updated", "Game state updated after solving message.");
    }

    // PRIVATE METHODS //
    private void sendGameStateUpdate(String gameId, String state, String message) {
        GameStateMessage gameStateMessage = new GameStateMessage(gameId, state, message, LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/game-status/" + gameId, gameStateMessage);
    }

    private ShopItem getShopItemById(String gameId, String itemId) {
        List<ShopItem> shopItems = mugloarService.getShopItems(gameId);
        Optional<ShopItem> optionalItem = shopItems.stream()
                .filter(item -> item.getId().equalsIgnoreCase(itemId))
                .findFirst();

        if (optionalItem.isPresent()) {
            return optionalItem.get();
        } else {
            LOGGER.warn("Shop item with ID '{}' not found in GameID={}.", itemId, gameId);
            sendGameStateUpdate(gameId, "shop_item_not_found", "Required shop item '" + itemId + "' not found.");
            return null;
        }
    }

    private void updateGameStateFromPurchaseResponse(Game game, ShopPurchaseResponse purchaseResponse,
                                                     String purchasedItemName) {
        if (purchaseResponse == null) {
            LOGGER.warn("Purchase response is null. No game state updated.");
            sendGameStateUpdate(game.getGameId(), "purchase_response_null", "Purchase response is null.");
            return;
        }

        game.setLives(purchaseResponse.getLives());
        game.setGold(purchaseResponse.getGold());
        game.setLevel(purchaseResponse.getLevel());
        game.setTurn(purchaseResponse.getTurn());

        applyItemEffect(game, purchasedItemName);

        LOGGER.debug("Game state updated after purchasing item. Current state: {}", game);
        sendGameStateUpdate(game.getGameId(), "purchase_updated", "Game state updated after purchasing item.");
    }

    private void applyItemEffect(Game game, String message) {
        if (message == null || message.isEmpty()) {
            LOGGER.warn("Purchase message is null or empty. No effect applied.");
            sendGameStateUpdate(game.getGameId(), "unknown_purchase_effect", "Purchase message is null or empty.");
            return;
        }

        if (message.contains("Healing Potion")) {
            game.setLives(game.getLives() + 1);
            LOGGER.debug("Applied effect of 'Healing Potion': Restored 1 life.");
            sendGameStateUpdate(game.getGameId(), "item_effect_applied", "Applied effect of 'Healing Potion': " +
                    "Restored 1 life.");
        } else if (message.contains("Potion of Awesome Wings")) {
            game.setWingStrength(game.getWingStrength() + 10);
            LOGGER.debug("Applied effect of 'Potion of Awesome Wings': Increased wingStrength by 10.");
            sendGameStateUpdate(game.getGameId(), "item_effect_applied", "Applied effect of 'Potion of Awesome " +
                    "Wings': Increased wingStrength by 10.");
        } else if (message.contains("Matrix")) {
            game.setCunning(game.getCunning() + 5);
            LOGGER.debug("Applied effect of 'Matrix': Increased cunning by 5.");
            sendGameStateUpdate(game.getGameId(), "item_effect_applied", "Applied effect of 'Matrix': Increased " +
                    "cunning by 5.");
        } else if (message.contains("Excalibur Sword")) {
            game.setFireBreath(game.getFireBreath() + 15);
            LOGGER.debug("Applied effect of 'Excalibur Sword': Increased fireBreath by 15.");
            sendGameStateUpdate(game.getGameId(), "item_effect_applied", "Applied effect of 'Excalibur Sword': " +
                    "Increased fireBreath by 15.");
        } else if (message.contains("Aegis Shield")) {
            game.setScaleThickness(game.getScaleThickness() + 10);
            LOGGER.debug("Applied effect of 'Aegis Shield': Increased scaleThickness by 10.");
            sendGameStateUpdate(game.getGameId(), "item_effect_applied", "Applied effect of 'Aegis Shield': Increased" +
                    " scaleThickness by 10.");
        } else {
            LOGGER.warn("Unknown item effect in message '{}'. No effect applied.", message);
            sendGameStateUpdate(game.getGameId(), "unknown_item_effect", message);
        }
    }
}
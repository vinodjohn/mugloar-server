package com.bigbank.mugloarserver.services.implementations;

import com.bigbank.mugloarserver.models.*;
import com.bigbank.mugloarserver.services.StrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of StrategyService
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Service
public class StrategyServiceImpl implements StrategyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyServiceImpl.class);

    private final Set<String> solvedMessageIds = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<String, Integer> failureCounts = new ConcurrentHashMap<>();
    private double peopleMultiplier = 1.0;
    private double stateMultiplier = 1.0;
    private double underworldMultiplier = 1.0;

    @Override
    public void processInvestigation(Investigation investigation) {
        LOGGER.info("Processing investigation results: {}", investigation);

        int people = investigation.getPeople();
        int state = investigation.getState();
        int underworld = investigation.getUnderworld();

        int total = people + state + underworld;

        if (total == 0) {
            LOGGER.warn("Investigation totals to zero. Setting default multipliers.");

            this.peopleMultiplier = 1.0;
            this.stateMultiplier = 1.0;
            this.underworldMultiplier = 1.0;

            return;
        }

        double peopleProportion = (double) people / total;
        double stateProportion = (double) state / total;
        double underworldProportion = (double) underworld / total;

        LOGGER.debug("Proportions - People: {}, State: {}, Underworld: {}",
                peopleProportion, stateProportion, underworldProportion);

        this.peopleMultiplier = peopleProportion;
        this.stateMultiplier = stateProportion;
        this.underworldMultiplier = underworldProportion;

        LOGGER.info("Adjusted multipliers - People: {}, State: {}, Underworld: {}",
                peopleMultiplier, stateMultiplier, underworldMultiplier);
    }

    @Override
    public Message chooseMessage(List<Message> messages, Game game) {
        if (messages == null || messages.isEmpty()) {
            LOGGER.info("No messages available to choose from.");
            return null;
        }

        List<Message> unsolvedMessages = messages.stream()
                .filter(message -> !solvedMessageIds.contains(message.getAdId()))
                .toList();

        if (unsolvedMessages.isEmpty()) {
            LOGGER.info("All messages have been solved.");
            return null;
        }

        List<MessageWithScore> scoredMessages = unsolvedMessages.stream()
                .map(message -> new MessageWithScore(message, computeDifficultyScore(message, game)))
                .toList();

        Optional<MessageWithScore> bestMessage = scoredMessages.stream()
                .max(Comparator.comparingDouble(messageWithScore -> messageWithScore.message().getIntReward() / messageWithScore.score()));

        if (bestMessage.isPresent()) {
            Message selectedMessage = bestMessage.get().message();

            LOGGER.debug("Chosen message ID: {} with difficulty score: {}", selectedMessage.getAdId(),
                    bestMessage.get().score());
            return selectedMessage;
        } else {
            LOGGER.info("No suitable messages found after scoring.");
            return null;
        }
    }

    @Override
    public List<ShopItem> decideItemsToBuy(Game game, List<ShopItem> shopItems) {
        if (shopItems == null || shopItems.isEmpty()) {
            LOGGER.info("No shop items available to buy.");
            return Collections.emptyList();
        }

        Set<String> requiredItemIds = new HashSet<>(Arrays.asList("hpot", "wingpot", "mtrix"));

        List<ShopItem> requiredItems = shopItems.stream()
                .filter(item -> requiredItemIds.contains(item.getId()))
                .toList();

        // Sort required items by descending score (higher score = higher priority)
        List<ShopItemWithScore> scoredRequiredItems = requiredItems.stream()
                .map(item -> new ShopItemWithScore(item, computeShopItemScore(item)))
                .sorted(Comparator.comparingDouble(ShopItemWithScore::score).reversed()).toList();

        List<ShopItem> selectedItems = new ArrayList<>();
        double remainingGold = game.getGold();

        for (ShopItemWithScore scoredItem : scoredRequiredItems) {
            ShopItem item = scoredItem.shopItem();

            if (item.getCost() <= remainingGold) {
                selectedItems.add(item);
                remainingGold -= item.getCost();

                LOGGER.debug("Selected required item '{}' for purchase. Remaining gold: {}",
                        item.getName(), remainingGold);
            }
        }

        double finalRemainingGold = remainingGold;
        List<ShopItem> additionalItems = shopItems.stream()
                .filter(item -> !requiredItemIds.contains(item.getId()))
                .sorted(Comparator.comparingDouble(this::computeBenefitPerGold).reversed())
                .filter(item -> item.getCost() <= finalRemainingGold)
                .toList();

        for (ShopItem item : additionalItems) {
            if (item.getCost() <= remainingGold) {
                selectedItems.add(item);
                remainingGold -= item.getCost();

                LOGGER.debug("Selected additional item '{}' for purchase. Remaining gold: {}",
                        item.getName(), remainingGold);
            }
        }

        if (selectedItems.isEmpty()) {
            LOGGER.info("No required or additional shop items selected based on current strategy.");
        } else {
            LOGGER.debug("Selected shop items to buy: {}", selectedItems);
        }

        return selectedItems;
    }

    @Override
    public void markMessageAsSolved(String adId) {
        if (adId == null || adId.isEmpty()) {
            LOGGER.warn("Attempted to mark an invalid message ID as solved: '{}'", adId);
            return;
        }

        solvedMessageIds.add(adId);
        LOGGER.debug("Marked message ID '{}' as solved.", adId);
    }

    @Override
    public void recordFailure(String adId) {
        failureCounts.put(adId, failureCounts.getOrDefault(adId, 0) + 1);
        LOGGER.debug("Recorded failure for message ID '{}'. Total failures: {}", adId, failureCounts.get(adId));
    }

    // PRIVATE METHODS //
    private double computeDifficultyScore(Message message, Game game) {
        int reward = message.getIntReward();
        int expiresIn = message.getExpiresIn();
        String category = message.getCategory();

        if (expiresIn <= 0) {
            LOGGER.warn("Message '{}' has non-positive expiresIn: {}. Assigning maximum difficulty.",
                    message.getAdId(), expiresIn);

            return Double.MAX_VALUE; // Highest difficulty
        }

        // Base difficulty score: higher reward and lower expiresIn imply easier messages
        double baseDifficulty = (double) reward / expiresIn;

        double categoryAdjustment = getCategoryAdjustment(category);

        double dragonLevel = game.getDragonLevel();

        double adjustedDifficulty =
                baseDifficulty * (peopleMultiplier + stateMultiplier + underworldMultiplier) * categoryAdjustment
                        / dragonLevel;

        int failures = failureCounts.getOrDefault(message.getAdId(), 0);
        adjustedDifficulty += failures * 0.5; // Example: each failure adds 0.5 to difficulty

        LOGGER.debug("Computed difficulty score for message '{}': {} (Category: {}, Adjustment: {}, Failures: {}, " +
                        "Dragon Level: {})",
                message.getAdId(), adjustedDifficulty, category, categoryAdjustment, failures, dragonLevel);

        return adjustedDifficulty;
    }

    private double getCategoryAdjustment(String category) {
        if (category == null) {
            LOGGER.warn("Message category is null. Using neutral adjustment.");
            return 1.0;
        }

        return switch (category.toLowerCase()) {
            case "negotiation" -> 0.9; // Easier
            case "combat" -> 1.2; // Harder
            case "exploration" -> 1.0; // Neutral
            default -> {
                LOGGER.warn("Unknown category '{}'. Using neutral adjustment.", category);
                yield 1.0;
            }
        };
    }

    private double computeBenefitPerGold(ShopItem item) {
        return switch (item.getId().toLowerCase()) {
            case "hpot" -> // Healing Potion
                    0.05;
            case "wingpot" -> // Potion of Awesome Wings
                    0.066;
            case "mtrix" -> // Matrix (Assumed to be an item that improves skills)
                    0.075;

            default -> 0.0;
        };
    }

    private double computeShopItemScore(ShopItem item) {
        double cost = item.getCost();

        if (cost <= 0) {
            LOGGER.warn("Shop item '{}' has non-positive cost: {}. Assigning minimum score.", item.getName(), cost);
            return 0.0;
        }

        // Formula: Higher cost and higher multipliers increase the score
        double score = cost * (peopleMultiplier + stateMultiplier + underworldMultiplier);

        LOGGER.debug("Computed score for shop item '{}': {}", item.getName(), score);

        return score;
    }
}
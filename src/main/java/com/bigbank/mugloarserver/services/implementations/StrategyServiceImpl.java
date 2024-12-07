package com.bigbank.mugloarserver.services.implementations;

import com.bigbank.mugloarserver.models.Investigation;
import com.bigbank.mugloarserver.models.Message;
import com.bigbank.mugloarserver.services.StrategyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Implementation of StrategyService
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Service
public class StrategyServiceImpl implements StrategyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StrategyServiceImpl.class);

    @Override
    public void processInvestigation(Investigation investigation) {
        if (investigation != null) {
            LOGGER.info("Investigation: people={}, state={}, underworld={}",
                    investigation.getPeople(), investigation.getState(), investigation.getUnderworld());
        }
    }

    @Override
    public Message chooseMessage(Message[] messages) {
        if (messages == null || messages.length == 0) {
            return null;
        }

        return Arrays.stream(messages)
                .map(this::tryParseReward)
                .filter(p -> p != null && p.parsedReward >= 0)
                .max(Comparator.comparingInt(a -> a.parsedReward))
                .map(p -> p.message)
                .orElse(null);
    }

    // PRIVATE METHODS //
    private ParsedMessage tryParseReward(Message message) {
        try {
            int r = Integer.parseInt(message.getReward().trim());
            return new ParsedMessage(message, r);
        } catch (NumberFormatException e) {
            LOGGER.warn("Cannot parse reward '{}' for message {}. Skipping.", message.getReward(), message.getAdId());
            return null;
        }
    }

    private record ParsedMessage(Message message, int parsedReward) {
    }
}

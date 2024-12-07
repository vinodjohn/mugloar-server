package com.bigbank.mugloarserver.services;

import com.bigbank.mugloarserver.models.Investigation;
import com.bigbank.mugloarserver.models.Message;

/**
 * Service interface defining strategy operations for choosing messages to solve and processing investigation results.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
public interface StrategyService {
    /**
     * Processes investigation results. Typically used to log or analyze
     * reputation data.
     *
     * @param inv the Investigation result
     */
    void processInvestigation(Investigation inv);

    /**
     * Chooses a single message from an array of messages based on some heuristic,
     * such as the highest reward.
     *
     * @param messages an array of available Messages
     * @return the chosen Message, or null if none are suitable
     */
    Message chooseMessage(Message[] messages);
}

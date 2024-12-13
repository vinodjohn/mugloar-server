package com.bigbank.mugloarserver.models;

/**
 * MessageWithScore model that associates a Message with a computed score.
 *
 * @author vinodjohn
 * @created 09.12.2024
 */
public record MessageWithScore(Message message, double score) {
}
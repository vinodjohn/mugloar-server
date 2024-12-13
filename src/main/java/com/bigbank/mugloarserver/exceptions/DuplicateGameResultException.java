package com.bigbank.mugloarserver.exceptions;

/**
 * Exception thrown when attempting to save a duplicate GameResult.
 *
 * @author vinodjohn
 * @created 09.12.2024
 */
public class DuplicateGameResultException extends RuntimeException {
    public DuplicateGameResultException(String message, Throwable cause) {
        super(message, cause);
    }
}
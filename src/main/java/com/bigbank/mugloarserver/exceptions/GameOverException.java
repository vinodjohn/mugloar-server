package com.bigbank.mugloarserver.exceptions;

/**
 * Exception thrown when a "Game Over" status is detected from an API response.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
public class GameOverException extends MugloarException {
    public GameOverException(String message) {
        super(message);
    }
}

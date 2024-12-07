package com.bigbank.mugloarserver.exceptions;

import lombok.Getter;

/**
 * Exception to handle Game's unavailability
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Getter
public class GameNotFoundException extends RuntimeException {
    private final String code;

    public GameNotFoundException(String code) {
        super(code);
        this.code = code;
    }
}

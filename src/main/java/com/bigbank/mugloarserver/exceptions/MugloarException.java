package com.bigbank.mugloarserver.exceptions;

import lombok.Getter;

/**
 * Exception to handle generic errors
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Getter
public class MugloarException extends RuntimeException {
    private final String code;

    public MugloarException(String code) {
        super(code);
        this.code = code;
    }
}

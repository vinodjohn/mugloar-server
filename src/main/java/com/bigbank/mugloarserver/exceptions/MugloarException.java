package com.bigbank.mugloarserver.exceptions;

import lombok.Getter;

/**
 * A general exception for various known application errors, identified by a message code.
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

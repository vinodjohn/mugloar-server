package com.bigbank.mugloarserver.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * GameStateMessage model
 *
 * @author vinodjohn
 * @created 13.12.2024
 */
@Data
@AllArgsConstructor
public class GameStateMessage {
    private String gameId;
    private String state;
    private String message;
    private LocalDateTime timestamp;
}

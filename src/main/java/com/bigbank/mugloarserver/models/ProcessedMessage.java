package com.bigbank.mugloarserver.models;

import jakarta.persistence.Embeddable;
import lombok.*;

/**
 * ProcessedMessage model that represents a solved message with relevant details.
 *
 * @author vinodjohn
 * @created 08.12.2024
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ProcessedMessage {
    private String decodedAdId;
    private String decodedMessage;
    private int turn;
    private int reward;
    private boolean success;
    private String failureReason;
}

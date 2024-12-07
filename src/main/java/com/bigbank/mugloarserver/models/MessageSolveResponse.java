package com.bigbank.mugloarserver.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * MessageSolveResponse model
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageSolveResponse {
    @JsonProperty("success")
    private boolean success;

    @JsonProperty("lives")
    private int lives;

    @JsonProperty("gold")
    private int gold;

    @JsonProperty("score")
    private int score;

    @JsonProperty("highScore")
    private int highScore;

    @JsonProperty("turn")
    private int turn;

    @JsonProperty("message")
    private String message;
}

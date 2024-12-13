package com.bigbank.mugloarserver.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MessageSolveResponse model that represents the response returned after attempting to solve a message (task).
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageSolveResponse {
    @JsonProperty("success")
    private boolean success;

    @JsonProperty("lives")
    private int lives;

    @JsonProperty("gold")
    private double gold;

    @JsonProperty("score")
    private int score;

    @JsonProperty("highScore")
    private int highScore;

    @JsonProperty("turn")
    private int turn;

    @JsonProperty("message")
    private String message;
}

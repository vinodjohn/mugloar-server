package com.bigbank.mugloarserver.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Game model that represents the state of a game as returned by the Dragons of Mugloar API.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Game {
    @JsonProperty("gameId")
    private String gameId;

    @JsonProperty("lives")
    private int lives;

    @JsonProperty("gold")
    private int gold;

    @JsonProperty("level")
    private int level;

    @JsonProperty("score")
    private int score;

    @JsonProperty("highScore")
    private int highScore;

    @JsonProperty("turn")
    private int turn;
}

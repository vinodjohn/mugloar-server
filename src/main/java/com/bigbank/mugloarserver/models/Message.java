package com.bigbank.mugloarserver.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Message model that represents a task (message) retrieved from the message board in the game.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {
    @JsonProperty("adId")
    private String adId;

    @JsonProperty("message")
    private String message;

    @JsonProperty("reward")
    private String reward;

    @JsonProperty("expiresIn")
    private int expiresIn;
}

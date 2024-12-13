package com.bigbank.mugloarserver.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;
import java.util.List;

/**
 * Message model that represents a task (message) retrieved from the message board in the game.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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

    @JsonProperty("encrypted")
    private Integer encrypted;

    @JsonProperty("probability")
    private String probability;

    private List<String> requiredItems;
    private String category;

    public String getDecodedAdId() {
        if (this.encrypted != null && this.encrypted == 1) {
            try {
                return new String(Base64.getDecoder().decode(this.adId));
            } catch (IllegalArgumentException e) {
                return "Invalid encoded adId.";
            }
        }
        return this.adId;
    }

    public String getDecodedMessage() {
        if (this.encrypted != null && this.encrypted == 1) {
            try {
                return new String(Base64.getDecoder().decode(this.message));
            } catch (IllegalArgumentException e) {
                return "Invalid encoded message.";
            }
        }
        return this.message;
    }

    public Integer getIntReward() {
        if (this.reward != null) {
            return Integer.parseInt(this.reward);
        }

        return null;
    }
}

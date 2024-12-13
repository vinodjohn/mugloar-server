package com.bigbank.mugloarserver.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Investigation model that represents the result of an investigation about the player's reputation.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Investigation {
    @JsonProperty("people")
    private int people;

    @JsonProperty("state")
    private int state;

    @JsonProperty("underworld")
    private int underworld;
}

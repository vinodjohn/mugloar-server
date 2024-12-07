package com.bigbank.mugloarserver.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Investigation model
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Investigation {
    @JsonProperty("people")
    private int people;

    @JsonProperty("state")
    private int state;

    @JsonProperty("underworld")
    private int underworld;
}

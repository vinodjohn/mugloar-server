package com.bigbank.mugloarserver.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * ShopPurchaseResponse model
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopPurchaseResponse {
    @JsonProperty("shoppingSuccess")
    private String shoppingSuccess;

    @JsonProperty("gold")
    private int gold;

    @JsonProperty("lives")
    private int lives;

    @JsonProperty("level")
    private int level;

    @JsonProperty("turn")
    private int turn;
}

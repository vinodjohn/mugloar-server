package com.bigbank.mugloarserver.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ShopPurchaseResponse model that represents the response after attempting to purchase an item from the shop.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ShopPurchaseResponse {
    @JsonProperty("shoppingSuccess")
    private String shoppingSuccess;

    @JsonProperty("gold")
    private double gold;

    @JsonProperty("lives")
    private int lives;

    @JsonProperty("level")
    private int level;

    @JsonProperty("turn")
    private int turn;

    public boolean isSuccess() {
        return Boolean.parseBoolean(shoppingSuccess);
    }
}

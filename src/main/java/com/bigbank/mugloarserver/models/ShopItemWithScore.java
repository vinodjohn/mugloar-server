package com.bigbank.mugloarserver.models;

/**
 * ShopItemWithScore model that associates a ShopItem with a computed score.
 *
 * @author vinodjohn
 * @created 13.12.2024
 */
public record ShopItemWithScore(ShopItem shopItem, double score) {
}

package com.bigbank.mugloarserver.services.implementations;

import com.bigbank.mugloarserver.models.ShopItem;
import com.bigbank.mugloarserver.services.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of InventoryService
 *
 * @author vinodjohn
 * @created 10.12.2024
 */
@Service
public class InventoryServiceImpl implements InventoryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final Map<String, List<ShopItem>> ownedItemsStore = new ConcurrentHashMap<>();

    @Override
    public boolean hasItem(String gameId, ShopItem item) {
        List<ShopItem> ownedItems = ownedItemsStore.getOrDefault(gameId, Collections.emptyList());
        return ownedItems.contains(item);
    }

    @Override
    public void addItem(String gameId, ShopItem item) {
        ownedItemsStore.computeIfAbsent(gameId, k -> new ArrayList<>()).add(item);
        LOGGER.debug("Added item '{}' to GameID={}.", item.getName(), gameId);
    }

    @Override
    public List<ShopItem> getAllByGameId(String gameId) {
        return ownedItemsStore.getOrDefault(gameId, Collections.emptyList());
    }
}
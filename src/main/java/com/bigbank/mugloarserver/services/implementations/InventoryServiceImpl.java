package com.bigbank.mugloarserver.services.implementations;

import com.bigbank.mugloarserver.services.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
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

    private final Map<String, Set<String>> ownedItemsStore = new ConcurrentHashMap<>();

    @Override
    public boolean hasItem(String gameId, String itemId) {
        Set<String> ownedItemIds = ownedItemsStore.getOrDefault(gameId, Collections.emptySet());
        return ownedItemIds.contains(itemId.toLowerCase());
    }

    public void addItem(String gameId, String itemId) {
        ownedItemsStore.computeIfAbsent(gameId, k -> ConcurrentHashMap.newKeySet())
                .add(itemId.toLowerCase());

        LOGGER.debug("Added item '{}' to GameID={}.", itemId, gameId);
    }
}
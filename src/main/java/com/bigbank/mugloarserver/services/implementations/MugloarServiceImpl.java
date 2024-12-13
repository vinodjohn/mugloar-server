package com.bigbank.mugloarserver.services.implementations;

import com.bigbank.mugloarserver.exceptions.GameOverException;
import com.bigbank.mugloarserver.exceptions.MugloarException;
import com.bigbank.mugloarserver.models.*;
import com.bigbank.mugloarserver.services.MugloarService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Implementation of MugloarService
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Service
public class MugloarServiceImpl implements MugloarService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MugloarServiceImpl.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public MugloarServiceImpl(@Value("${mugloar.api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Game startGame() {
        LOGGER.debug("Starting a new game..");
        Game game = post("/game/start", Game.class);
        validateGame(game);
        return game;
    }

    @Override
    public Investigation investigate(String gameId) {
        LOGGER.debug("Investigating reputation for gameId: {}", gameId);

        validateGameId(gameId);
        Investigation investigation = post("/{gameId}/investigate/reputation", Investigation.class, gameId);

        if (investigation == null) {
            throw new MugloarException("error.unexpected");
        }

        return investigation;
    }

    @Override
    public List<Message> getMessages(String gameId) {
        LOGGER.debug("Retrieving messages for gameId: {}", gameId);

        validateGameId(gameId);
        List<Message> messages = get("/{gameId}/messages", gameId, new ParameterizedTypeReference<>() {
        });

        if (messages == null) {
            throw new MugloarException("error.unexpected");
        } else {
            for (Message message : messages) {
                validateMessage(message);
            }

        }

        return messages;
    }

    @Override
    public MessageSolveResponse solveMessage(String gameId, String adId) {
        LOGGER.debug("Solving message adId: {} for gameId: {}", adId, gameId);

        validateGameId(gameId);
        validateNotBlank(adId);
        MessageSolveResponse messageSolveResponse = post("/{gameId}/solve/{adId}", MessageSolveResponse.class,
                gameId, adId);

        if (messageSolveResponse == null) {
            throw new MugloarException("error.unexpected");
        }

        return messageSolveResponse;
    }

    @Override
    public List<ShopItem> getShopItems(String gameId) {
        LOGGER.debug("Retrieving shop items for gameId: {}", gameId);

        validateGameId(gameId);
        List<ShopItem> shopItems = get("/{gameId}/shop", gameId, new ParameterizedTypeReference<>() {
        });

        if (shopItems == null) {
            throw new MugloarException("error.unexpected");
        } else {
            for (ShopItem shopItem : shopItems) {
                validateShopItem(shopItem);
            }

        }

        return shopItems;
    }

    @Override
    public ShopPurchaseResponse buyItem(String gameId, String itemId) {
        LOGGER.debug("Attempting to buy itemId: {} for gameId: {}", itemId, gameId);

        validateGameId(gameId);
        validateNotBlank(itemId);
        ShopPurchaseResponse shopPurchaseResponse = post("/{gameId}/shop/buy/{itemId}", ShopPurchaseResponse.class,
                gameId, itemId);

        if (shopPurchaseResponse == null) {
            throw new MugloarException("error.unexpected");
        }

        return shopPurchaseResponse;
    }

    // PRIVATE METHODS //
    private void validateGameId(String gameId) {
        validateNotBlank(gameId);
    }

    private void validateGame(Game game) {
        if (game == null || game.getGameId() == null || game.getGameId().isBlank()) {
            throw new MugloarException("error.unexpected");
        }
    }

    private void validateMessage(Message message) {
        if (message == null || message.getDecodedAdId() == null || message.getDecodedAdId().isBlank() ||
                message.getIntReward() <= 0 || message.getDecodedMessage() == null || message.getDecodedMessage().isBlank()) {
            throw new MugloarException("error.invalid.input");
        }
    }

    private void validateShopItem(ShopItem item) {
        if (item == null || item.getId() == null || item.getId().isBlank() || item.getName() == null ||
                item.getName().isBlank() || item.getCost() <= 0) {
            throw new MugloarException("error.invalid.input");
        }
    }

    private void validateNotBlank(String val) {
        if (val == null || val.isBlank()) {
            throw new MugloarException("error.invalid.input");
        }
    }

    private String resolveUri(String uriTemplate, Object... variables) {
        if (variables == null || variables.length == 0) {
            return uriTemplate;
        }

        return String.format(uriTemplate.replaceAll("\\{[^}]+}", "%s"), variables);
    }

    private boolean isGameOver(String rawResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(rawResponse);

            if (rootNode.has("status") && "Game Over".equalsIgnoreCase(rootNode.get("status").asText())) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing JSON response: {}", e.getMessage(), e);
        }

        return false;
    }

    private <T> T get(String uriTemplate, String gameId, ParameterizedTypeReference<T> responseType) {
        try {
            Mono<String> responseMono = webClient.get()
                    .uri(uriTemplate, gameId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        String resolvedUri = resolveUri(uriTemplate, gameId);
                                        if (isGameOver(body)) {
                                            LOGGER.warn("Game Over detected in response from {}: {}", resolvedUri,
                                                    body);
                                            return Mono.error(new GameOverException("Received 'Game Over' status from" +
                                                    " GET " + resolvedUri));
                                        }
                                        LOGGER.error("Error response from {}: {}", resolvedUri, body);
                                        return Mono.error(new MugloarException("error.unexpected"));
                                    }))
                    .bodyToMono(String.class);

            String rawResponse = responseMono.blockOptional().orElseThrow(() -> new MugloarException("error" +
                    ".unexpected"));

            if (isGameOver(rawResponse)) {
                throw new GameOverException("Received 'Game Over' status from GET " + resolveUri(uriTemplate, gameId));
            }

            return objectMapper.readValue(rawResponse,
                    objectMapper.getTypeFactory().constructType(responseType.getType()));
        } catch (GameOverException goe) {
            LOGGER.warn(goe.getMessage());
            throw goe;
        } catch (Exception e) {
            String resolvedUri = resolveUri(uriTemplate, gameId);
            LOGGER.error("GET request to {} failed: {}", resolvedUri, e.getMessage(), e);
            throw new MugloarException("error.unexpected", e);
        }
    }

    private <T> T post(String uriTemplate, Class<T> responseType, Object... uriVariables) {
        try {
            Mono<String> responseMono = webClient.post()
                    .uri(uriTemplate, uriVariables)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class)
                                    .flatMap(body -> {
                                        String resolvedUri = resolveUri(uriTemplate, uriVariables);
                                        if (isGameOver(body)) {
                                            LOGGER.warn("Game Over detected in response from {}: {}", resolvedUri,
                                                    body);
                                            return Mono.error(new GameOverException("Received 'Game Over' status from" +
                                                    " POST " + resolvedUri));
                                        }
                                        LOGGER.error("Error response from {}: {}", resolvedUri, body);
                                        return Mono.error(new MugloarException("error.unexpected"));
                                    }))
                    .bodyToMono(String.class);

            String rawResponse = responseMono.blockOptional().orElseThrow(() -> new MugloarException("error" +
                    ".unexpected"));

            if (isGameOver(rawResponse)) {
                throw new GameOverException("Received 'Game Over' status from POST " + resolveUri(uriTemplate,
                        uriVariables));
            }

            return objectMapper.readValue(rawResponse, responseType);
        } catch (GameOverException goe) {
            LOGGER.warn(goe.getMessage());
            throw goe;
        } catch (Exception e) {
            String resolvedUri = resolveUri(uriTemplate, uriVariables);
            LOGGER.error("POST request to {} failed: {}", resolvedUri, e.getMessage(), e);
            throw new MugloarException("error.unexpected", e);
        }
    }
}
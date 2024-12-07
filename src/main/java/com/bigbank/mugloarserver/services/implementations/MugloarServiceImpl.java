package com.bigbank.mugloarserver.services.implementations;

import com.bigbank.mugloarserver.exceptions.MugloarException;
import com.bigbank.mugloarserver.models.*;
import com.bigbank.mugloarserver.services.MugloarService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Implementation of MugloarService
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Service
public class MugloarServiceImpl implements MugloarService {
    private final WebClient webClient;

    public MugloarServiceImpl(@Value("${mugloar.api.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public Game startGame() {
        Game game = post("/game/start", Game.class);
        validateGame(game);

        return game;
    }

    @Override
    public Investigation investigate(String gameId) {
        validateGameId(gameId);
        Investigation investigation = post("/{gameId}/investigate/reputation", gameId, Investigation.class);

        if (investigation == null) {
            throw new MugloarException("error.unexpected");
        }

        return investigation;
    }

    @Override
    public Message[] getMessages(String gameId) {
        validateGameId(gameId);
        Message[] messages = get("/{gameId}/messages", gameId, Message[].class);

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
        validateGameId(gameId);
        validateNotBlank(adId);

        MessageSolveResponse messageSolveResponse = post("/{gameId}/solve/{adId}", gameId, adId,
                MessageSolveResponse.class);

        if (messageSolveResponse == null) {
            throw new MugloarException("error.unexpected");
        }

        return messageSolveResponse;
    }

    @Override
    public ShopItem[] getShopItems(String gameId) {
        validateGameId(gameId);
        ShopItem[] shopItems = get("/{gameId}/shop", gameId, ShopItem[].class);

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
        validateGameId(gameId);
        validateNotBlank(itemId);

        ShopPurchaseResponse shopPurchaseResponse = post("/{gameId}/shop/buy/{itemId}", gameId, itemId,
                ShopPurchaseResponse.class);

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
        if (message == null || message.getAdId() == null || message.getAdId().isBlank() ||
                message.getReward() == null || message.getReward().isBlank()) {
            throw new MugloarException("error.invalid.input");
        }
    }

    private void validateShopItem(ShopItem shopItem) {
        if (shopItem == null || shopItem.getId() == null || shopItem.getId().isBlank()) {
            throw new MugloarException("error.invalid.input");
        }
    }

    private void validateNotBlank(String val) {
        if (val == null || val.isBlank()) throw new MugloarException("error.invalid.input");
    }

    private <T> T get(String uriTemplate, String gameId, Class<T> responseType) {
        return webClient.get().uri(uriTemplate, gameId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new MugloarException("error.unexpected"))))
                .bodyToMono(responseType)
                .blockOptional()
                .orElseThrow(() -> new MugloarException("error.unexpected"));
    }

    private <T> T post(String uriTemplate, Class<T> responseType) {
        return webClient.post().uri(uriTemplate)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new MugloarException("error.unexpected"))))
                .bodyToMono(responseType)
                .blockOptional()
                .orElseThrow(() -> new MugloarException("error.unexpected"));
    }

    private <T> T post(String uriTemplate, String gameId, Class<T> responseType) {
        return webClient.post().uri(uriTemplate, gameId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new MugloarException("error.unexpected"))))
                .bodyToMono(responseType)
                .blockOptional()
                .orElseThrow(() -> new MugloarException("error.unexpected"));
    }

    private <T> T post(String uriTemplate, String gameId, String param, Class<T> responseType) {
        return webClient.post().uri(uriTemplate, gameId, param)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.bodyToMono(String.class)
                        .flatMap(body -> Mono.error(new MugloarException("error.unexpected"))))
                .bodyToMono(responseType)
                .blockOptional()
                .orElseThrow(() -> new MugloarException("error.unexpected"));
    }
}
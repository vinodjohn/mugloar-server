package com.bigbank.mugloarserver.services.unit;

import com.bigbank.mugloarserver.exceptions.MugloarException;
import com.bigbank.mugloarserver.models.*;
import com.bigbank.mugloarserver.services.implementations.MugloarServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MugloarService
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
public class MugloarServiceTest {
    @InjectMocks
    MugloarServiceImpl mugloarService;

    WebClient webClient;

    WebClient.RequestHeadersUriSpec<?> getSpec;
    WebClient.RequestBodyUriSpec postSpec;
    WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webClient = mock(WebClient.class, RETURNS_DEEP_STUBS);
        mugloarService = new MugloarServiceImpl("http://localhost");
        mugloarService.webClient = webClient;
        getSpec = mock(WebClient.RequestHeadersUriSpec.class, RETURNS_DEEP_STUBS);
        postSpec = mock(WebClient.RequestBodyUriSpec.class, RETURNS_DEEP_STUBS);
        responseSpec = mock(WebClient.ResponseSpec.class, RETURNS_DEEP_STUBS);
    }

    @Test
    void startGameSuccess() {
        String rawResponse = "{\"gameId\":\"g123\",\"lives\":3,\"gold\":100.0,\"level\":1,\"score\":0," +
                "\"highScore\":0,\"turn\":0}";

        when(webClient.post()
                .uri(eq("/game/start"))
                .retrieve()
                .onStatus(any(), any())
                .bodyToMono(String.class)
                .blockOptional())
                .thenReturn(Optional.of(rawResponse));

        Game g = mugloarService.startGame();

        assertEquals("g123", g.getGameId());
    }

    @Test
    void startGameNull() {
        mockPostNull("/game/start");
        assertThrows(MugloarException.class, () -> mugloarService.startGame());
    }

    @Test
    void investigateSuccess() {
        when(webClient.post().uri(eq("/{gameId}/investigate/reputation"), (Object[]) any()).retrieve()
                .onStatus(any(), any())
                .bodyToMono(String.class)
                .blockOptional())
                .thenReturn(Optional.of("{\"people\":5,\"state\":5,\"underworld\":10}"));

        Investigation i = mugloarService.investigate("test");
        assertEquals(5, i.getPeople());
    }

    @Test
    void investigateNull() {
        mockPostNull("/{gameId}/investigate/reputation");
        assertThrows(MugloarException.class, () -> mugloarService.investigate("test"));
    }

    @Test
    void getMessagesSuccess() {
        mockGet("/{gameId}/messages", "[{\"adId\":\"ad1\",\"message\":\"msg\",\"reward\":10,\"expiresIn\":3," +
                "\"probability\":\"Sure\"}]");
        List<Message> messages = mugloarService.getMessages("test");
        assertEquals(1, messages.size());
        assertEquals("ad1", messages.getFirst().getDecodedAdId());
    }

    @Test
    void getMessagesNull() {
        mockGetNull("/{gameId}/messages");
        assertThrows(MugloarException.class, () -> mugloarService.getMessages("test"));
    }

    @Test
    void solveMessageSuccess() {
        String rawResponse = "{\"success\":true,\"lives\":3,\"gold\":120.0,\"score\":50,\"highScore\":100,\"turn\":1}";

        when(webClient.post()
                .uri(eq("/{gameId}/solve/{adId}"), eq("test"), eq("adX"))
                .retrieve()
                .onStatus(any(), any())
                .bodyToMono(String.class)
                .blockOptional())
                .thenReturn(Optional.of(rawResponse));

        MessageSolveResponse r = mugloarService.solveMessage("test", "adX");
        assertTrue(r.isSuccess());
        assertEquals(3, r.getLives());
    }

    @Test
    void solveMessageNull() {
        mockPostNull("/{gameId}/solve/{adId}");
        assertThrows(MugloarException.class, () -> mugloarService.solveMessage("test", "adX"));
    }

    @Test
    void getShopItemsSuccess() {
        mockGet("/{gameId}/shop", "[{\"id\":\"hpot\",\"name\":\"Healing Potion\",\"cost\":50.0}]");
        List<ShopItem> items = mugloarService.getShopItems("test");
        assertEquals(1, items.size());
        assertEquals("hpot", items.getFirst().getId());
    }

    @Test
    void getShopItemsNull() {
        mockGetNull("/{gameId}/shop");
        assertThrows(MugloarException.class, () -> mugloarService.getShopItems("test"));
    }

    @Test
    void buyItemSuccess() {
        String rawResponse = "{\"shoppingSuccess\":\"SUCCESS\",\"gold\":70.0,\"lives\":3,\"level\":2," +
                "\"turn\":2}";

        when(webClient.post()
                .uri(eq("/{gameId}/shop/buy/{itemId}"), eq("test"), eq("hpot"))
                .retrieve()
                .onStatus(any(), any())
                .bodyToMono(String.class)
                .blockOptional())
                .thenReturn(Optional.of(rawResponse));

        ShopPurchaseResponse r = mugloarService.buyItem("test", "hpot");
        assertEquals("SUCCESS", r.getShoppingSuccess());
    }

    @Test
    void buyItemNull() {
        mockPostNull("/{gameId}/shop/buy/{itemId}");
        assertThrows(MugloarException.class, () -> mugloarService.buyItem("test", "hpot"));
    }

    @Test
    void isGameOverTrue() throws Exception {
        Method m = MugloarServiceImpl.class.getDeclaredMethod("isGameOver", String.class);
        m.setAccessible(true);
        boolean result = (boolean) m.invoke(mugloarService, "{\"status\":\"Game Over\"}");
        assertTrue(result);
    }

    @Test
    void isGameOverFalse() throws Exception {
        Method m = MugloarServiceImpl.class.getDeclaredMethod("isGameOver", String.class);
        m.setAccessible(true);
        boolean result = (boolean) m.invoke(mugloarService, "{\"status\":\"ongoing\"}");
        assertFalse(result);
    }

    @Test
    void validateGameId_Blank() {
        assertThrows(MugloarException.class, () -> invokePrivate("validateGameId", ""));
    }

    @Test
    void validateGame_Null() {
        assertThrows(RuntimeException.class, () -> invokePrivate("validateGame", null));
    }

    @Test
    void validateMessage_Invalid() {
        Message msg = new Message(null, null, "-1", 0, null, "", Collections.emptyList(), "");
        assertThrows(MugloarException.class, () -> invokePrivate("validateMessage", msg));
    }

    @Test
    void validateShopItem_Invalid() {
        ShopItem si = new ShopItem(null, null, -1.0);
        assertThrows(MugloarException.class, () -> invokePrivate("validateShopItem", si));
    }

    // PRIVATE METHODS //
    private void mockGet(String uri, String raw) {
        when(webClient.get().uri(eq(uri), eq("test")).retrieve().onStatus(any(), any()).bodyToMono(String.class).blockOptional())
                .thenReturn(Optional.of(raw));
    }

    private void mockGetNull(String uri) {
        when(webClient.get().uri(eq(uri), eq("test")).retrieve().onStatus(any(), any()).bodyToMono(String.class).blockOptional())
                .thenReturn(Optional.empty());
    }

    private void mockPostNull(String uri) {
        when(webClient.post().uri(eq(uri), (Object[]) any()).retrieve()
                .onStatus(any(), any())
                .bodyToMono(String.class)
                .blockOptional())
                .thenReturn(Optional.empty());
    }

    private void invokePrivate(String methodName, Object arg) {
        try {
            Method m = arg instanceof Game ? MugloarServiceImpl.class.getDeclaredMethod(methodName, Game.class)
                    : arg instanceof Message ? MugloarServiceImpl.class.getDeclaredMethod(methodName, Message.class)
                    : arg instanceof ShopItem ? MugloarServiceImpl.class.getDeclaredMethod(methodName, ShopItem.class)
                    : MugloarServiceImpl.class.getDeclaredMethod(methodName, String.class);
            m.setAccessible(true);
            m.invoke(mugloarService, arg);
        } catch (Exception e) {
            if (e.getCause() != null) throw (RuntimeException) e.getCause();
            throw new RuntimeException(e);
        }
    }
}
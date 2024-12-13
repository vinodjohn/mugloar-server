package com.bigbank.mugloarserver.controllers;

import com.bigbank.mugloarserver.facades.GameFacade;
import com.bigbank.mugloarserver.models.Game;
import com.bigbank.mugloarserver.models.GameResult;
import com.bigbank.mugloarserver.services.GameResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller handling web requests for starting a game and viewing game history.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Controller
@RequestMapping("/game")
public class GameController {
    private final ConcurrentHashMap<String, Game> activeGames = new ConcurrentHashMap<>();

    @Autowired
    private GameFacade gameFacade;
    @Autowired
    private GameResultService gameResultService;

    @ResponseBody
    @PostMapping("/start")
    public ResponseEntity<?> startGame() {
        Game game = gameFacade.initializeGame();

        if (game == null) {
            return ResponseEntity.status(500).body("{\"error\": \"Failed to start the game.\"}");
        }

        String gameId = game.getGameId();

        activeGames.put(gameId, game);

        CompletableFuture.runAsync(() -> {
            gameFacade.playGame(game);
            activeGames.remove(gameId);
        });

        return ResponseEntity.ok().body("{\"gameId\": \"" + gameId + "\"}");
    }

    @GetMapping("/{id}")
    public String displayResult(Model model, @PathVariable String id) {
        GameResult gameResult = gameResultService.findByGameId(id);

        if (gameResult != null) {
            model.addAttribute("gameResult", gameResult);
            model.addAttribute("processedMessages", gameResult.getProcessedMessages());
            return "result";
        } else {
            model.addAttribute("errorMessage", "Failed to retrieve game results.");
            return "error";
        }
    }

    @GetMapping("/history")
    public String getGameHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "error", required = false) String error,
            Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<GameResult> gameResultPage = gameResultService.getAllGameResults(pageable);
        model.addAttribute("gameResultPage", gameResultPage);

        if ("notfound".equals(error)) {
            model.addAttribute("errorMessage", "Failed to retrieve game history.");
            return "error";
        }

        return "history";
    }
}
package com.bigbank.mugloarserver.controllers;

import com.bigbank.mugloarserver.facades.GameFacade;
import com.bigbank.mugloarserver.models.Game;
import com.bigbank.mugloarserver.models.GameResult;
import com.bigbank.mugloarserver.services.GameResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controller handling web requests for starting a game and viewing game history.
 *
 * @author vinodjohn
 * @created 07.12.2024
 */
@Controller
@RequestMapping("/game")
public class GameController {
    @Autowired
    private GameFacade gameFacade;

    @Autowired
    private GameResultService gameResultService;

    @GetMapping
    public String startGame(Model model) {
        Game finalGame = gameFacade.playGame();
        boolean achievedGoal = finalGame.getScore() >= 1000;

        model.addAttribute("score", finalGame.getScore());
        model.addAttribute("highScore", finalGame.getHighScore());
        model.addAttribute("lives", finalGame.getLives());
        model.addAttribute("achievedGoal", achievedGoal);

        return "result";
    }

    @GetMapping("/history")
    public String history(Model model) {
        List<GameResult> results = gameResultService.findAll();
        model.addAttribute("results", results);

        return "history";
    }
}

package com.bigbank.mugloarserver.services.unit;

import com.bigbank.mugloarserver.exceptions.DuplicateGameResultException;
import com.bigbank.mugloarserver.models.GameResult;
import com.bigbank.mugloarserver.repositories.GameResultRepository;
import com.bigbank.mugloarserver.services.implementations.GameResultServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GameResultService
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
public class GameResultServiceTest {
    @Mock
    private GameResultRepository gameResultRepository;

    @InjectMocks
    private GameResultServiceImpl gameResultService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void save_Success() throws DuplicateGameResultException {
        GameResult gr = new GameResult();
        gr.setGameId("testId");
        gameResultService.save(gr);
        verify(gameResultRepository).saveAndFlush(gr);
    }

    @Test
    void save_DuplicateGameResultException() {
        GameResult gr = new GameResult();
        gr.setGameId("duplicateId");
        doThrow(new DataIntegrityViolationException("Duplicate")).when(gameResultRepository).saveAndFlush(gr);
        assertThrows(DuplicateGameResultException.class, () -> gameResultService.save(gr));
    }

    @Test
    void save_OtherException() {
        GameResult gr = new GameResult();
        gr.setGameId("errorId");
        doThrow(new RuntimeException("DB error")).when(gameResultRepository).saveAndFlush(gr);
        assertThrows(RuntimeException.class, () -> gameResultService.save(gr));
    }

    @Test
    void findByGameId_Success() {
        GameResult gr = new GameResult();
        gr.setGameId("foundId");
        when(gameResultRepository.findByGameId("foundId")).thenReturn(gr);
        GameResult result = gameResultService.findByGameId("foundId");
        assertEquals("foundId", result.getGameId());
    }

    @Test
    void findByGameId_NotFound() {
        when(gameResultRepository.findByGameId("notFound")).thenReturn(null);
        GameResult result = gameResultService.findByGameId("notFound");
        assertNull(result);
    }

    @Test
    void findByGameId_Exception() {
        when(gameResultRepository.findByGameId("err")).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> gameResultService.findByGameId("err"));
    }

    @Test
    void getAllGameResults_Success() {
        Page<GameResult> page = new PageImpl<>(Collections.emptyList());
        when(gameResultRepository.findAll(any(Pageable.class))).thenReturn(page);
        Page<GameResult> result = gameResultService.getAllGameResults(PageRequest.of(0, 10));
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void getAllGameResults_Exception() {
        when(gameResultRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> gameResultService.getAllGameResults(PageRequest.of(0, 10)));
    }
}

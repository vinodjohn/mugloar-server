package com.bigbank.mugloarserver.controllers.unit;

import com.bigbank.mugloarserver.controllers.HomeController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for Home controller
 *
 * @author vinodjohn
 * @created 14.12.2024
 */
public class HomeControllerTest {
    @InjectMocks
    private HomeController homeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void home_ReturnsIndex() {
        String viewName = homeController.home();
        assertEquals("index", viewName);
    }

    @Test
    void home_NotNull() {
        String viewName = homeController.home();
        assertEquals("index", viewName);
    }
}

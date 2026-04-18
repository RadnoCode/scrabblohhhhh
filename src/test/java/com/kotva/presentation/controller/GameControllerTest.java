package com.kotva.presentation.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GameControllerTest {

    @Test
    public void calculateElapsedMillisReturnsZeroWhenClockBaselineIsUninitialized() {
        assertEquals(0L, GameController.calculateElapsedMillis(0L, 5_000_000_000L));
        assertEquals(0L, GameController.calculateElapsedMillis(-1L, 5_000_000_000L));
    }

    @Test
    public void calculateElapsedMillisReturnsZeroWhenCurrentTickDoesNotAdvance() {
        assertEquals(0L, GameController.calculateElapsedMillis(2_000_000L, 2_000_000L));
        assertEquals(0L, GameController.calculateElapsedMillis(3_000_000L, 2_000_000L));
    }

    @Test
    public void calculateElapsedMillisReturnsWholeMillisecondsForAdvancedClock() {
        assertEquals(2L, GameController.calculateElapsedMillis(1_000_000L, 3_999_999L));
        assertEquals(15L, GameController.calculateElapsedMillis(10_000_000L, 25_400_000L));
    }
}

package com.vrtrading.trademanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Utils Tests")
class UtilsTest {

    @Test
    @DisplayName("Should return LONG direction for LONG trade type")
    void testGetTradeDirectionForLong() {
        TradeDirection direction = Utils.getTradeDirection(TradeType.LONG);
        assertEquals(TradeDirection.LONG, direction, "LONG trade type should return LONG direction");
    }

    @Test
    @DisplayName("Should return SHORT direction for SHORT trade type")
    void testGetTradeDirectionForShort() {
        TradeDirection direction = Utils.getTradeDirection(TradeType.SHORT);
        assertEquals(TradeDirection.SHORT, direction, "SHORT trade type should return SHORT direction");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for LONG_EXIT trade type")
    void testGetTradeDirectionThrowsForLongExit() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Utils.getTradeDirection(TradeType.LONG_EXIT);
        }, "LONG_EXIT should throw IllegalArgumentException");
        
        assertTrue(exception.getMessage().contains("Unexpected TradeType"),
            "Exception message should mention unexpected TradeType");
        assertTrue(exception.getMessage().contains("LONG_EXIT"),
            "Exception message should include the trade type");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for SHORT_EXIT trade type")
    void testGetTradeDirectionThrowsForShortExit() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            Utils.getTradeDirection(TradeType.SHORT_EXIT);
        }, "SHORT_EXIT should throw IllegalArgumentException");
        
        assertTrue(exception.getMessage().contains("Unexpected TradeType"),
            "Exception message should mention unexpected TradeType");
        assertTrue(exception.getMessage().contains("SHORT_EXIT"),
            "Exception message should include the trade type");
    }
}

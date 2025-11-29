package com.vrtading.trademanager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TradeType Tests")
class TradeTypeTest {

    @Test
    @DisplayName("Should identify LONG as entry trade type")
    void testLongIsEntryTradeType() {
        assertTrue(TradeType.LONG.isEntryTradeType(), "LONG should be an entry trade type");
        assertFalse(TradeType.LONG.isExitTradeType(), "LONG should not be an exit trade type");
    }

    @Test
    @DisplayName("Should identify SHORT as entry trade type")
    void testShortIsEntryTradeType() {
        assertTrue(TradeType.SHORT.isEntryTradeType(), "SHORT should be an entry trade type");
        assertFalse(TradeType.SHORT.isExitTradeType(), "SHORT should not be an exit trade type");
    }

    @Test
    @DisplayName("Should identify LONG_EXIT as exit trade type")
    void testLongExitIsExitTradeType() {
        assertTrue(TradeType.LONG_EXIT.isExitTradeType(), "LONG_EXIT should be an exit trade type");
        assertFalse(TradeType.LONG_EXIT.isEntryTradeType(), "LONG_EXIT should not be an entry trade type");
    }

    @Test
    @DisplayName("Should identify SHORT_EXIT as exit trade type")
    void testShortExitIsExitTradeType() {
        assertTrue(TradeType.SHORT_EXIT.isExitTradeType(), "SHORT_EXIT should be an exit trade type");
        assertFalse(TradeType.SHORT_EXIT.isEntryTradeType(), "SHORT_EXIT should not be an entry trade type");
    }

    @ParameterizedTest
    @EnumSource(value = TradeType.class, names = {"LONG", "SHORT"})
    @DisplayName("Should correctly identify all entry trade types")
    void testAllEntryTradeTypes(TradeType tradeType) {
        assertTrue(tradeType.isEntryTradeType(), tradeType + " should be an entry trade type");
    }

    @ParameterizedTest
    @EnumSource(value = TradeType.class, names = {"LONG_EXIT", "SHORT_EXIT"})
    @DisplayName("Should correctly identify all exit trade types")
    void testAllExitTradeTypes(TradeType tradeType) {
        assertTrue(tradeType.isExitTradeType(), tradeType + " should be an exit trade type");
    }

    @Test
    @DisplayName("Should convert from string using valueOf")
    void testValueOf() {
        assertEquals(TradeType.LONG, TradeType.valueOf("LONG"));
        assertEquals(TradeType.SHORT, TradeType.valueOf("SHORT"));
        assertEquals(TradeType.LONG_EXIT, TradeType.valueOf("LONG_EXIT"));
        assertEquals(TradeType.SHORT_EXIT, TradeType.valueOf("SHORT_EXIT"));
    }

    @Test
    @DisplayName("Should throw exception for invalid trade type string")
    void testValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> {
            TradeType.valueOf("INVALID");
        });
    }

    @Test
    @DisplayName("Entry and exit trade types should be mutually exclusive")
    void testMutualExclusivity() {
        for (TradeType type : TradeType.values()) {
            boolean isEntry = type.isEntryTradeType();
            boolean isExit = type.isExitTradeType();
            
            // Each type should be either entry or exit, but not both
            assertTrue(isEntry ^ isExit, 
                type + " should be either entry or exit, but not both");
        }
    }

    @Test
    @DisplayName("Should have correct string representation")
    void testToString() {
        assertEquals("LONG", TradeType.LONG.toString());
        assertEquals("SHORT", TradeType.SHORT.toString());
        assertEquals("LONG_EXIT", TradeType.LONG_EXIT.toString());
        assertEquals("SHORT_EXIT", TradeType.SHORT_EXIT.toString());
    }
}


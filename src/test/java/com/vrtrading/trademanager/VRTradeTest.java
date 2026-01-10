package com.vrtrading.trademanager;

import com.mytrading.utils.DecimalValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VRTrade Tests")
class VRTradeTest {

    private VRTrade createTestTrade(TradeType type) {
        StopLoss stopLoss = new StopLoss(StopLoss.SLType.Absolute, new DecimalValue(100));

        return new VRTrade(
            "TestBroker",
            "INST123",
            "NSE",
            "EQ",
            OffsetDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneOffset.ofHours(5)),
            "TRADE001",
            type,
            new DecimalValue(100),
            new DecimalValue(1500.50),
            stopLoss
        );
    }

    @Test
    @DisplayName("LONG trade should be identified as entry trade")
    void testLongIsEntryTrade() {
        VRTrade trade = createTestTrade(TradeType.LONG);
        assertTrue(trade.isEntryTrade(), "LONG trade should be an entry trade");
    }

    @Test
    @DisplayName("SHORT trade should be identified as entry trade")
    void testShortIsEntryTrade() {
        VRTrade trade = createTestTrade(TradeType.SHORT);
        assertTrue(trade.isEntryTrade(), "SHORT trade should be an entry trade");
    }

    @Test
    @DisplayName("LONG_EXIT trade should not be identified as entry trade")
    void testLongExitIsNotEntryTrade() {
        VRTrade trade = createTestTrade(TradeType.LONG_EXIT);
        assertFalse(trade.isEntryTrade(), "LONG_EXIT trade should not be an entry trade");
    }

    @Test
    @DisplayName("SHORT_EXIT trade should not be identified as entry trade")
    void testShortExitIsNotEntryTrade() {
        VRTrade trade = createTestTrade(TradeType.SHORT_EXIT);
        assertFalse(trade.isEntryTrade(), "SHORT_EXIT trade should not be an entry trade");
    }
}

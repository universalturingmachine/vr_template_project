package com.vrtrading.trademanager;

import com.mytrading.utils.Bar;
import com.mytrading.utils.DecimalValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TradeSequence Tests")
class TradeSequenceTest {

    private TradeSequence tradeSequence;
    private OffsetDateTime timestamp;

    @BeforeEach
    void setUp() {
        TradeDirection tradeDirection = TradeDirection.LONG;
        tradeSequence = new TradeSequence(tradeDirection);
        timestamp = OffsetDateTime.of(2024, 1, 15, 10, 30, 0, 0, ZoneOffset.ofHours(5));
    }

    private VRTrade createTrade(TradeType type, double quantity, double price) {
        StopLoss stopLoss = new StopLoss(StopLoss.SLType.Absolute, new DecimalValue(100));

        return new VRTrade(
            "TestBroker",
            "INST123",
            "NSE",
            "EQ",
            timestamp,
            "TRADE" + System.nanoTime(),
            type,
            new DecimalValue(quantity),
            new DecimalValue(price),
            stopLoss
        );
    }

    private Bar createBar(double open, double high, double low, double close) {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 5);
        return new Bar(open, high, low, close, 1000.0, startTime, endTime);
    }

    @Test
    @DisplayName("New TradeSequence should be active")
    void testNewSequenceIsActive() {
        assertTrue(tradeSequence.isActive(), "New TradeSequence should be active");
    }

    @Test
    @DisplayName("New TradeSequence should have zero outstanding shares")
    void testNewSequenceHasZeroOutstandingShares() {
        DecimalValue outstanding = tradeSequence.getTotalOutstandingShares();
        assertEquals(0.0, outstanding.value(), 0.001, "New sequence should have 0 outstanding shares");
    }

    @Test
    @DisplayName("New TradeSequence should have correct trade direction")
    void testNewSequenceHasCorrectTradeDirection() {
        assertEquals(TradeDirection.LONG, tradeSequence.getTradeDirection(),
            "TradeSequence should have LONG direction");
    }

    @Test
    @DisplayName("TradeSequence with SHORT direction should be created correctly")
    void testShortTradeSequence() {
        TradeSequence shortSequence = new TradeSequence(TradeDirection.SHORT);
        assertEquals(TradeDirection.SHORT, shortSequence.getTradeDirection(),
            "TradeSequence should have SHORT direction");
        assertTrue(shortSequence.isActive(), "New SHORT sequence should be active");
        assertEquals(0.0, shortSequence.getTotalOutstandingShares().value(), 0.001);
    }

    @Test
    @DisplayName("Adding LONG entry trade should increase outstanding shares")
    void testAddLongEntryTrade() {
        VRTrade longTrade = createTrade(TradeType.LONG, 100, 1500.00);
        tradeSequence.addTrade(longTrade);

        DecimalValue outstanding = tradeSequence.getTotalOutstandingShares();
        assertEquals(100.0, outstanding.value(), 0.001, "Outstanding shares should be 100");
        assertTrue(tradeSequence.isActive(), "Sequence should still be active");
    }

    @Test
    @DisplayName("Adding SHORT entry trade should increase outstanding shares")
    void testAddShortEntryTrade() {
        VRTrade shortTrade = createTrade(TradeType.SHORT, 50, 2000.00);
        tradeSequence.addTrade(shortTrade);

        DecimalValue outstanding = tradeSequence.getTotalOutstandingShares();
        assertEquals(50.0, outstanding.value(), 0.001, "Outstanding shares should be 50");
        assertTrue(tradeSequence.isActive(), "Sequence should still be active");
    }

    @Test
    @DisplayName("Adding multiple entry trades should accumulate outstanding shares")
    void testAddMultipleEntryTrades() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG, 50, 1510.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG, 25, 1520.00));

        DecimalValue outstanding = tradeSequence.getTotalOutstandingShares();
        assertEquals(175.0, outstanding.value(), 0.001, "Outstanding shares should be 175");
        assertTrue(tradeSequence.isActive(), "Sequence should still be active");
    }

    @Test
    @DisplayName("Adding exit trade should decrease outstanding shares")
    void testAddExitTrade() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 50, 1550.00));

        DecimalValue outstanding = tradeSequence.getTotalOutstandingShares();
        assertEquals(50.0, outstanding.value(), 0.001, "Outstanding shares should be 50");
        assertTrue(tradeSequence.isActive(), "Sequence should still be active");
    }

    @Test
    @DisplayName("Sequence should become inactive when all shares are exited")
    void testSequenceBecomesInactiveWhenFullyExited() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        DecimalValue outstanding = tradeSequence.getTotalOutstandingShares();
        assertEquals(0.0, outstanding.value(), 0.001, "Outstanding shares should be 0");
        assertFalse(tradeSequence.isActive(), "Sequence should be inactive");
    }

    @Test
    @DisplayName("Sequence should become inactive with multiple exit trades")
    void testSequenceBecomesInactiveWithMultipleExits() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 50, 1550.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 30, 1560.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 20, 1570.00));

        DecimalValue outstanding = tradeSequence.getTotalOutstandingShares();
        assertEquals(0.0, outstanding.value(), 0.001, "Outstanding shares should be 0");
        assertFalse(tradeSequence.isActive(), "Sequence should be inactive");
    }

    @Test
    @DisplayName("Should throw exception when adding trade to inactive sequence")
    void testAddTradeToInactiveSequence() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        assertFalse(tradeSequence.isActive(), "Sequence should be inactive");

        assertThrows(RuntimeException.class, () -> {
            tradeSequence.addTrade(createTrade(TradeType.LONG, 50, 1600.00));
        }, "Should throw exception when adding to inactive sequence");
    }

    @Test
    @DisplayName("getTotalOutstandingShares should return a clone")
    void testGetTotalOutstandingSharesReturnsClone() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));

        DecimalValue outstanding1 = tradeSequence.getTotalOutstandingShares();
        DecimalValue outstanding2 = tradeSequence.getTotalOutstandingShares();

        assertNotSame(outstanding1, outstanding2, "Should return different instances (clones)");
        assertEquals(outstanding1.value(), outstanding2.value(), 0.001, "Clones should have same value");
    }

    @Test
    @DisplayName("Should handle SHORT trades correctly")
    void testShortTrades() {
        TradeSequence shortSequence = new TradeSequence(TradeDirection.SHORT);
        shortSequence.addTrade(createTrade(TradeType.SHORT, 100, 2000.00));
        assertEquals(100.0, shortSequence.getTotalOutstandingShares().value(), 0.001);

        shortSequence.addTrade(createTrade(TradeType.SHORT_EXIT, 100, 1950.00));
        assertEquals(0.0, shortSequence.getTotalOutstandingShares().value(), 0.001);
        assertFalse(shortSequence.isActive());
    }

    @Test
    @DisplayName("TradeDirection should remain constant throughout sequence lifecycle")
    void testTradeDirectionRemainsConstant() {
        assertEquals(TradeDirection.LONG, tradeSequence.getTradeDirection());

        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        assertEquals(TradeDirection.LONG, tradeSequence.getTradeDirection(),
            "Direction should not change after adding entry trade");

        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 50, 1550.00));
        assertEquals(TradeDirection.LONG, tradeSequence.getTradeDirection(),
            "Direction should not change after adding exit trade");

        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 50, 1560.00));
        assertEquals(TradeDirection.LONG, tradeSequence.getTradeDirection(),
            "Direction should not change even when sequence becomes inactive");
        assertFalse(tradeSequence.isActive());
    }

    @Test
    @DisplayName("Should handle mixed entry and exit trades")
    void testMixedEntryAndExitTrades() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        assertEquals(100.0, tradeSequence.getTotalOutstandingShares().value(), 0.001);

        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 50, 1550.00));
        assertEquals(50.0, tradeSequence.getTotalOutstandingShares().value(), 0.001);

        tradeSequence.addTrade(createTrade(TradeType.LONG, 75, 1560.00));
        assertEquals(125.0, tradeSequence.getTotalOutstandingShares().value(), 0.001);

        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 125, 1580.00));
        assertEquals(0.0, tradeSequence.getTotalOutstandingShares().value(), 0.001);
        assertFalse(tradeSequence.isActive());
    }

    @Test
    @DisplayName("Should handle fractional quantities")
    void testFractionalQuantities() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100.5, 1500.00));
        assertEquals(100.5, tradeSequence.getTotalOutstandingShares().value(), 0.001);

        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 50.25, 1550.00));
        assertEquals(50.25, tradeSequence.getTotalOutstandingShares().value(), 0.001);

        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 50.25, 1560.00));
        assertEquals(0.0, tradeSequence.getTotalOutstandingShares().value(), 0.001);
        assertFalse(tradeSequence.isActive());
    }

    @Test
    @DisplayName("log method should not throw exception")
    void testLogMethod() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 50, 1550.00));

        assertDoesNotThrow(() -> tradeSequence.log(), "log() should not throw exception");
    }

    @Test
    @DisplayName("Should handle single trade that fully exits")
    void testSingleTradeFullExit() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        assertFalse(tradeSequence.isActive(), "Sequence should be inactive after full exit");
        assertEquals(0.0, tradeSequence.getTotalOutstandingShares().value(), 0.001);
    }

    @Test
    @DisplayName("Should handle partial exits correctly")
    void testPartialExits() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 25, 1550.00));
        assertEquals(75.0, tradeSequence.getTotalOutstandingShares().value(), 0.001);
        assertTrue(tradeSequence.isActive());

        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 25, 1560.00));
        assertEquals(50.0, tradeSequence.getTotalOutstandingShares().value(), 0.001);
        assertTrue(tradeSequence.isActive());
    }

    @Test
    @DisplayName("Should handle large quantities")
    void testLargeQuantities() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 10000, 1500.00));
        assertEquals(10000.0, tradeSequence.getTotalOutstandingShares().value(), 0.001);

        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 10000, 1550.00));
        assertEquals(0.0, tradeSequence.getTotalOutstandingShares().value(), 0.001);
        assertFalse(tradeSequence.isActive());
    }

    @Test
    @DisplayName("New sequence should have initial min/max price values")
    void testInitialMinMaxPrices() {
        assertEquals(Double.MAX_VALUE, tradeSequence.getMinPrice(), 0.001);
        assertEquals(Double.MIN_VALUE, tradeSequence.getMaxPrice(), 0.001);
    }

    @Test
    @DisplayName("newBarArrived should update minPrice when bar low is lower")
    void testNewBarArrivedUpdatesMinPrice() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));

        tradeSequence.newBarArrived(createBar(1500, 1520, 1480, 1510));
        assertEquals(1480.0, tradeSequence.getMinPrice(), 0.001);

        tradeSequence.newBarArrived(createBar(1510, 1530, 1470, 1520));
        assertEquals(1470.0, tradeSequence.getMinPrice(), 0.001);
    }

    @Test
    @DisplayName("newBarArrived should update maxPrice when bar high is higher")
    void testNewBarArrivedUpdatesMaxPrice() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));

        tradeSequence.newBarArrived(createBar(1500, 1550, 1480, 1510));
        assertEquals(1550.0, tradeSequence.getMaxPrice(), 0.001);

        tradeSequence.newBarArrived(createBar(1510, 1580, 1490, 1520));
        assertEquals(1580.0, tradeSequence.getMaxPrice(), 0.001);
    }

    @Test
    @DisplayName("newBarArrived should update both min and max prices")
    void testNewBarArrivedUpdatesBothMinMax() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));

        tradeSequence.newBarArrived(createBar(1500, 1550, 1480, 1510));
        assertEquals(1480.0, tradeSequence.getMinPrice(), 0.001);
        assertEquals(1550.0, tradeSequence.getMaxPrice(), 0.001);

        tradeSequence.newBarArrived(createBar(1510, 1600, 1450, 1520));
        assertEquals(1450.0, tradeSequence.getMinPrice(), 0.001);
        assertEquals(1600.0, tradeSequence.getMaxPrice(), 0.001);
    }

    @Test
    @DisplayName("newBarArrived should not change min/max if bar is within range")
    void testNewBarArrivedNoChangeIfWithinRange() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));

        tradeSequence.newBarArrived(createBar(1500, 1600, 1400, 1510));
        assertEquals(1400.0, tradeSequence.getMinPrice(), 0.001);
        assertEquals(1600.0, tradeSequence.getMaxPrice(), 0.001);

        tradeSequence.newBarArrived(createBar(1510, 1550, 1450, 1520));
        assertEquals(1400.0, tradeSequence.getMinPrice(), 0.001, "Min should not change");
        assertEquals(1600.0, tradeSequence.getMaxPrice(), 0.001, "Max should not change");
    }

    @Test
    @DisplayName("newBarArrived should throw exception on inactive sequence")
    void testNewBarArrivedThrowsOnInactiveSequence() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeSequence.addTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        assertFalse(tradeSequence.isActive(), "Sequence should be inactive");

        assertThrows(RuntimeException.class, () -> {
            tradeSequence.newBarArrived(createBar(1500, 1550, 1480, 1510));
        }, "Should throw exception when calling newBarArrived on inactive sequence");
    }

    @Test
    @DisplayName("newBarArrived should handle multiple bars correctly")
    void testNewBarArrivedMultipleBars() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));

        tradeSequence.newBarArrived(createBar(1500, 1520, 1490, 1510));
        tradeSequence.newBarArrived(createBar(1510, 1530, 1485, 1520));
        tradeSequence.newBarArrived(createBar(1520, 1560, 1500, 1550));

        assertEquals(1485.0, tradeSequence.getMinPrice(), 0.001);
        assertEquals(1560.0, tradeSequence.getMaxPrice(), 0.001);
    }

    @Test
    @DisplayName("newBarArrived should work with active sequence")
    void testNewBarArrivedOnActiveSequence() {
        tradeSequence.addTrade(createTrade(TradeType.LONG, 100, 1500.00));
        assertTrue(tradeSequence.isActive());

        assertDoesNotThrow(() -> {
            tradeSequence.newBarArrived(createBar(1500, 1550, 1480, 1510));
        }, "Should not throw exception on active sequence");
    }
}

package com.vrtading.trademanager;

import com.mytrading.utils.Bar;
import com.mytrading.utils.DecimalValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TradeGroup Tests")
class TradeGroupTest {

    private TradeGroup tradeGroup;
    private OffsetDateTime timestamp;

    @BeforeEach
    void setUp() {
        tradeGroup = new TradeGroup();
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

    @SuppressWarnings("unchecked")
    private List<TradeSequence> getTradeSequences() throws Exception {
        Field field = TradeGroup.class.getDeclaredField("tradeSequences");
        field.setAccessible(true);
        return (List<TradeSequence>) field.get(tradeGroup);
    }

    private Bar createBar(double open, double high, double low, double close) {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 1, 15, 10, 5);
        return new Bar(open, high, low, close, 1000.0, startTime, endTime);
    }

    @Test
    @DisplayName("New TradeGroup should have empty sequence list")
    void testNewTradeGroupIsEmpty() throws Exception {
        List<TradeSequence> sequences = getTradeSequences();
        assertTrue(sequences.isEmpty(), "New TradeGroup should have no sequences");
    }

    @Test
    @DisplayName("First trade should create a new sequence")
    void testFirstTradeCreatesSequence() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size(), "Should have exactly 1 sequence");
        assertTrue(sequences.get(0).isActive(), "First sequence should be active");
    }

    @Test
    @DisplayName("Multiple entry trades should use same sequence")
    void testMultipleEntryTradesUseSameSequence() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 50, 1510.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 25, 1520.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size(), "Should have exactly 1 sequence for all entry trades");
        assertTrue(sequences.get(0).isActive(), "Sequence should be active");
    }

    @Test
    @DisplayName("Should accept SHORT entry trades")
    void testShortEntryTrades() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 100, 2000.00));
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 50, 1990.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size(), "Should have exactly 1 sequence");
        assertTrue(sequences.get(0).isActive(), "Sequence should be active");
    }

    @Test
    @DisplayName("Should accept exit trades in same sequence")
    void testExitTradesInSameSequence() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 50, 1550.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size(), "Should have exactly 1 sequence");
    }

    @Test
    @DisplayName("Should handle mixed LONG and LONG_EXIT trades")
    void testMixedLongTrades() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 50, 1510.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 75, 1550.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 25, 1560.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size(), "Should have exactly 1 sequence");
    }

    @Test
    @DisplayName("Should handle mixed SHORT and SHORT_EXIT trades")
    void testMixedShortTrades() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 100, 2000.00));
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 50, 1990.00));
        tradeGroup.enterTrade(createTrade(TradeType.SHORT_EXIT, 75, 1950.00));
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 25, 1940.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size(), "Should have exactly 1 sequence");
    }

    @Test
    @DisplayName("Should handle single trade entry")
    void testSingleTradeEntry() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size(), "Should have exactly 1 sequence");
        assertTrue(sequences.get(0).isActive(), "Sequence should be active");
    }

    @Test
    @DisplayName("Should handle fractional quantities")
    void testFractionalQuantities() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100.5, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 50.25, 1550.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size(), "Should have exactly 1 sequence");
    }

    @Test
    @DisplayName("Should handle large number of trades")
    void testLargeNumberOfTrades() throws Exception {
        for (int i = 0; i < 100; i++) {
            tradeGroup.enterTrade(createTrade(TradeType.LONG, 10, 1500.00 + i));
        }
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size(), "Should have exactly 1 sequence for all trades");
    }

    @Test
    @DisplayName("enterTrade should not throw exception for valid trades")
    void testEnterTradeDoesNotThrow() {
        assertDoesNotThrow(() -> {
            tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
            tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));
            tradeGroup.enterTrade(createTrade(TradeType.SHORT, 50, 2000.00));
            tradeGroup.enterTrade(createTrade(TradeType.SHORT_EXIT, 50, 1950.00));
        }, "enterTrade should not throw exception for valid trades");
    }

    @Test
    @DisplayName("Should create new sequence when previous becomes inactive")
    void testNewSequenceWhenPreviousInactive() throws Exception {
        // Complete first sequence
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));
        
        // Start new sequence
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 50, 1600.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(2, sequences.size(), "Should have 2 sequences");
        assertFalse(sequences.get(0).isActive(), "First sequence should be inactive");
        assertTrue(sequences.get(1).isActive(), "Second sequence should be active");
        assertEquals(50.0, sequences.get(1).getTotalOutstandingShares().value(), 0.001);
    }
    
    @Test
    @DisplayName("Should create multiple sequences as they complete")
    void testMultipleSequences() throws Exception {
        // Sequence 1
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));
        
        // Sequence 2
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 50, 2000.00));
        tradeGroup.enterTrade(createTrade(TradeType.SHORT_EXIT, 50, 1950.00));
        
        // Sequence 3
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 75, 1600.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(3, sequences.size(), "Should have 3 sequences");
        assertFalse(sequences.get(0).isActive(), "Sequence 1 should be inactive");
        assertFalse(sequences.get(1).isActive(), "Sequence 2 should be inactive");
        assertTrue(sequences.get(2).isActive(), "Sequence 3 should be active");
        assertEquals(75.0, sequences.get(2).getTotalOutstandingShares().value(), 0.001);
    }

    @Test
    @DisplayName("Should handle complex sequence transitions")
    void testComplexSequenceTransitions() throws Exception {
        // Sequence 1: Partial exit, then complete
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 50, 1510.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 75, 1550.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 75, 1560.00));
        
        // Sequence 2: New entry after first completes
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 200, 2000.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(2, sequences.size(), "Should have 2 sequences");
        assertFalse(sequences.get(0).isActive(), "First sequence should be inactive");
        assertTrue(sequences.get(1).isActive(), "Second sequence should be active");
        assertEquals(200.0, sequences.get(1).getTotalOutstandingShares().value(), 0.001);
    }

    @Test
    @DisplayName("Should verify outstanding shares across sequences")
    void testOutstandingSharesAcrossSequences() throws Exception {
        // Sequence 1
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));
        
        // Sequence 2
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 200, 1600.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 50, 1650.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(0.0, sequences.get(0).getTotalOutstandingShares().value(), 0.001, 
            "First sequence should have 0 outstanding shares");
        assertEquals(150.0, sequences.get(1).getTotalOutstandingShares().value(), 0.001, 
            "Second sequence should have 150 outstanding shares");
    }

    @Test
    @DisplayName("Should handle alternating complete sequences")
    void testAlternatingCompleteSequences() throws Exception {
        // Sequence 1
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        // Sequence 2
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 50, 2000.00));
        tradeGroup.enterTrade(createTrade(TradeType.SHORT_EXIT, 50, 1950.00));

        // Sequence 3
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 75, 1600.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 75, 1650.00));

        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(3, sequences.size(), "Should have 3 sequences");
        assertFalse(sequences.get(0).isActive(), "All sequences should be inactive");
        assertFalse(sequences.get(1).isActive(), "All sequences should be inactive");
        assertFalse(sequences.get(2).isActive(), "All sequences should be inactive");
    }

    @Test
    @DisplayName("newBarArrived should do nothing when no active sequence")
    void testNewBarArrivedWithNoActiveSequence() throws Exception {
        // No trades yet
        assertDoesNotThrow(() -> {
            tradeGroup.newBarArrived(createBar(1500, 1550, 1480, 1510));
        }, "Should not throw when no sequences exist");

        List<TradeSequence> sequences = getTradeSequences();
        assertTrue(sequences.isEmpty(), "Should still have no sequences");
    }

    @Test
    @DisplayName("newBarArrived should do nothing when last sequence is inactive")
    void testNewBarArrivedWithInactiveSequence() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        assertDoesNotThrow(() -> {
            tradeGroup.newBarArrived(createBar(1500, 1550, 1480, 1510));
        }, "Should not throw when last sequence is inactive");
    }

    @Test
    @DisplayName("newBarArrived should update active sequence")
    void testNewBarArrivedUpdatesActiveSequence() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));

        tradeGroup.newBarArrived(createBar(1500, 1550, 1480, 1510));

        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size());
        assertEquals(1480.0, sequences.get(0).getMinPrice(), 0.001);
        assertEquals(1550.0, sequences.get(0).getMaxPrice(), 0.001);
    }

    @Test
    @DisplayName("newBarArrived should update only the last active sequence")
    void testNewBarArrivedUpdatesOnlyLastSequence() throws Exception {
        // Sequence 1 - complete
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.newBarArrived(createBar(1500, 1520, 1490, 1510));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        // Sequence 2 - active
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 50, 1600.00));
        tradeGroup.newBarArrived(createBar(1600, 1650, 1580, 1620));

        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(2, sequences.size());

        // First sequence should have its original min/max
        assertEquals(1490.0, sequences.get(0).getMinPrice(), 0.001);
        assertEquals(1520.0, sequences.get(0).getMaxPrice(), 0.001);

        // Second sequence should have the new bar's min/max
        assertEquals(1580.0, sequences.get(1).getMinPrice(), 0.001);
        assertEquals(1650.0, sequences.get(1).getMaxPrice(), 0.001);
    }

    @Test
    @DisplayName("newBarArrived should handle multiple bars on active sequence")
    void testNewBarArrivedMultipleBars() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));

        tradeGroup.newBarArrived(createBar(1500, 1520, 1490, 1510));
        tradeGroup.newBarArrived(createBar(1510, 1530, 1485, 1520));
        tradeGroup.newBarArrived(createBar(1520, 1560, 1500, 1550));

        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1485.0, sequences.get(0).getMinPrice(), 0.001);
        assertEquals(1560.0, sequences.get(0).getMaxPrice(), 0.001);
    }

    @Test
    @DisplayName("newBarArrived should work across sequence transitions")
    void testNewBarArrivedAcrossSequenceTransitions() throws Exception {
        // Sequence 1
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.newBarArrived(createBar(1500, 1550, 1480, 1510));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        // After sequence 1 completes, newBarArrived should do nothing
        tradeGroup.newBarArrived(createBar(1550, 1600, 1530, 1580));

        // Sequence 2
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 50, 2000.00));
        tradeGroup.newBarArrived(createBar(2000, 2050, 1980, 2020));

        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(2, sequences.size());

        // Sequence 1 should not have been affected by the bar after it completed
        assertEquals(1480.0, sequences.get(0).getMinPrice(), 0.001);
        assertEquals(1550.0, sequences.get(0).getMaxPrice(), 0.001);

        // Sequence 2 should have its bar data
        assertEquals(1980.0, sequences.get(1).getMinPrice(), 0.001);
        assertEquals(2050.0, sequences.get(1).getMaxPrice(), 0.001);
    }
}


package com.vrtrading.trademanager;

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
        assertTrue(sequences.getFirst().isActive(), "First sequence should be active");
    }

    @Test
    @DisplayName("First LONG trade should create sequence with LONG direction")
    void testFirstLongTradeCreatesLongSequence() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));

        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size());
        assertEquals(TradeDirection.LONG, sequences.getFirst().getTradeDirection(),
            "Sequence should have LONG direction");
    }

    @Test
    @DisplayName("First SHORT trade should create sequence with SHORT direction")
    void testFirstShortTradeCreatesShortSequence() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 100, 2000.00));

        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size());
        assertEquals(TradeDirection.SHORT, sequences.getFirst().getTradeDirection(),
            "Sequence should have SHORT direction");
    }

    @Test
    @DisplayName("Multiple entry trades should use same sequence")
    void testMultipleEntryTradesUseSameSequence() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 50, 1510.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 25, 1520.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size(), "Should have exactly 1 sequence for all entry trades");
        assertTrue(sequences.getFirst().isActive(), "Sequence should be active");
    }

    @Test
    @DisplayName("Should accept SHORT entry trades")
    void testShortEntryTrades() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 100, 2000.00));
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 50, 1990.00));
        
        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(1, sequences.size(), "Should have exactly 1 sequence");
        assertTrue(sequences.getFirst().isActive(), "Sequence should be active");
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
        assertTrue(sequences.getFirst().isActive(), "Sequence should be active");
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
    @DisplayName("New sequence after completion should use direction from new entry trade")
    void testNewSequenceUsesNewTradeDirection() throws Exception {
        // First sequence: LONG
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        // Second sequence: SHORT
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 50, 2000.00));

        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(2, sequences.size());
        assertEquals(TradeDirection.LONG, sequences.get(0).getTradeDirection(),
            "First sequence should be LONG");
        assertEquals(TradeDirection.SHORT, sequences.get(1).getTradeDirection(),
            "Second sequence should be SHORT");
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

        // Verify directions
        assertEquals(TradeDirection.LONG, sequences.get(0).getTradeDirection());
        assertEquals(TradeDirection.SHORT, sequences.get(1).getTradeDirection());
        assertEquals(TradeDirection.LONG, sequences.get(2).getTradeDirection());
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

        // Verify alternating directions
        assertEquals(TradeDirection.LONG, sequences.get(0).getTradeDirection());
        assertEquals(TradeDirection.SHORT, sequences.get(1).getTradeDirection());
        assertEquals(TradeDirection.LONG, sequences.get(2).getTradeDirection());
    }

    @Test
    @DisplayName("Should handle consecutive LONG sequences")
    void testConsecutiveLongSequences() throws Exception {
        // Sequence 1: LONG
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        // Sequence 2: LONG
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 50, 1600.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 50, 1650.00));

        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(2, sequences.size());
        assertEquals(TradeDirection.LONG, sequences.get(0).getTradeDirection());
        assertEquals(TradeDirection.LONG, sequences.get(1).getTradeDirection());
    }

    @Test
    @DisplayName("Should handle consecutive SHORT sequences")
    void testConsecutiveShortSequences() throws Exception {
        // Sequence 1: SHORT
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 100, 2000.00));
        tradeGroup.enterTrade(createTrade(TradeType.SHORT_EXIT, 100, 1950.00));

        // Sequence 2: SHORT
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 50, 1900.00));
        tradeGroup.enterTrade(createTrade(TradeType.SHORT_EXIT, 50, 1850.00));

        List<TradeSequence> sequences = getTradeSequences();
        assertEquals(2, sequences.size());
        assertEquals(TradeDirection.SHORT, sequences.get(0).getTradeDirection());
        assertEquals(TradeDirection.SHORT, sequences.get(1).getTradeDirection());
    }

    @Test
    @DisplayName("getActiveTradeSequence should return null when no sequences exist")
    void testGetActiveTradeSequenceWhenEmpty() {
        TradeSequence activeSequence = tradeGroup.getActiveTradeSequence();
        assertNull(activeSequence, "Should return null when no sequences exist");
    }

    @Test
    @DisplayName("getActiveTradeSequence should return the active sequence")
    void testGetActiveTradeSequenceWhenActive() throws Exception {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));

        TradeSequence activeSequence = tradeGroup.getActiveTradeSequence();
        assertNotNull(activeSequence, "Should return active sequence");
        assertTrue(activeSequence.isActive(), "Returned sequence should be active");
        assertEquals(100.0, activeSequence.getTotalOutstandingShares().value(), 0.001);

        // Verify it's the same as the last sequence
        List<TradeSequence> sequences = getTradeSequences();
        assertSame(sequences.getLast(), activeSequence,
            "Should return the same instance as the last sequence");
    }

    @Test
    @DisplayName("getActiveTradeSequence should return null when last sequence is inactive")
    void testGetActiveTradeSequenceWhenInactive() {
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        TradeSequence activeSequence = tradeGroup.getActiveTradeSequence();
        assertNull(activeSequence, "Should return null when last sequence is inactive");
    }

    @Test
    @DisplayName("getActiveTradeSequence should return the last active sequence when multiple exist")
    void testGetActiveTradeSequenceWithMultipleSequences() throws Exception {
        // First sequence (will be inactive)
        tradeGroup.enterTrade(createTrade(TradeType.LONG, 100, 1500.00));
        tradeGroup.enterTrade(createTrade(TradeType.LONG_EXIT, 100, 1550.00));

        // Second sequence (active)
        tradeGroup.enterTrade(createTrade(TradeType.SHORT, 50, 2000.00));

        TradeSequence activeSequence = tradeGroup.getActiveTradeSequence();
        assertNotNull(activeSequence, "Should return the active sequence");
        assertTrue(activeSequence.isActive(), "Returned sequence should be active");
        assertEquals(TradeDirection.SHORT, activeSequence.getTradeDirection(),
            "Should return the SHORT sequence");
        assertEquals(50.0, activeSequence.getTotalOutstandingShares().value(), 0.001);

        // Verify it's the last sequence, not the first
        List<TradeSequence> sequences = getTradeSequences();
        assertSame(sequences.getLast(), activeSequence,
            "Should return the last sequence");
        assertNotSame(sequences.getFirst(), activeSequence,
            "Should not return the first (inactive) sequence");
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
        assertEquals(1480.0, sequences.getFirst().getMinPrice(), 0.001);
        assertEquals(1550.0, sequences.getFirst().getMaxPrice(), 0.001);
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
        assertEquals(1485.0, sequences.getFirst().getMinPrice(), 0.001);
        assertEquals(1560.0, sequences.getFirst().getMaxPrice(), 0.001);
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


package com.vrtading.trademanager;

import com.mytrading.utils.DecimalValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
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
}


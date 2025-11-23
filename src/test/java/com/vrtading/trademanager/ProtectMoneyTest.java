package com.vrtading.trademanager;

import com.mytrading.utils.Kvp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProtectMoney Tests")
class ProtectMoneyTest {

    @Test
    @DisplayName("Should not allow instantiation of utility class")
    void testConstructorThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> {
            // Use reflection to access private constructor
            var constructor = ProtectMoney.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        });

        // The actual exception is wrapped in InvocationTargetException
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause(), "Should throw UnsupportedOperationException when trying to instantiate");
    }

    @ParameterizedTest
    @DisplayName("Should calculate stop loss for LONG trades correctly")
    @CsvSource({
        // For LONG: stopLoss = min(price * 0.99, price - 3000/lotSize)
        "100, 100, 70.0",      // price=100, lotSize=100 -> min(99.0, 70.0) = 70.0
        "200, 50, 140.0",      // price=200, lotSize=50 -> min(198.0, 140.0) = 140.0
        "500, 25, 380.0",      // price=500, lotSize=25 -> min(495.0, 380.0) = 380.0
        "1000, 10, 700.0",     // price=1000, lotSize=10 -> min(990.0, 700.0) = 700.0
        "50, 100, 20.0",       // price=50, lotSize=100 -> min(49.5, 20.0) = 20.0
        "10000, 100, 9900.0"   // price=10000, lotSize=100 -> min(9900.0, 9970.0) = 9900.0
    })
    void testGetStopLossForLong(double price, int lotSize, double expectedStopLoss) {
        double actualStopLoss = ProtectMoney.getStopLoss(TradeType.LONG, lotSize, price);
        assertEquals(expectedStopLoss, actualStopLoss, 0.01,
            String.format("Stop loss for LONG at price %.2f with lotSize %d should be %.2f",
                price, lotSize, expectedStopLoss));
    }

    @ParameterizedTest
    @DisplayName("Should calculate stop loss for SHORT trades correctly")
    @CsvSource({
        // For SHORT: stopLoss = min(price * 1.01, price + 3000/lotSize)
        "100, 100, 101.0",     // price=100, lotSize=100 -> min(101.0, 130.0) = 101.0
        "200, 50, 202.0",      // price=200, lotSize=50 -> min(202.0, 260.0) = 202.0
        "500, 25, 505.0",      // price=500, lotSize=25 -> min(505.0, 620.0) = 505.0
        "1000, 10, 1010.0",    // price=1000, lotSize=10 -> min(1010.0, 1300.0) = 1010.0
        "50, 100, 50.5",       // price=50, lotSize=100 -> min(50.5, 80.0) = 50.5
        "10000, 100, 10030.0"  // price=10000, lotSize=100 -> min(10100.0, 10030.0) = 10030.0
    })
    void testGetStopLossForShort(double price, int lotSize, double expectedStopLoss) {
        double actualStopLoss = ProtectMoney.getStopLoss(TradeType.SHORT, lotSize, price);
        assertEquals(expectedStopLoss, actualStopLoss, 0.01,
            String.format("Stop loss for SHORT at price %.2f with lotSize %d should be %.2f",
                price, lotSize, expectedStopLoss));
    }

    @Test
    @DisplayName("Should use minimum of percent and absolute stop loss for LONG")
    void testGetStopLossUsesMinimumForLong() {
        // With stopLossPercent = 1% and stopLossAbsolute = 3000
        // For LONG: percent gives price * 0.99, absolute gives price - (3000/lotSize)

        // Case 1: Absolute stop loss is smaller (should be used)
        // price=100, lotSize=10 -> percent: 99.0, absolute: 100 - 300 = -200
        // Min should be -200.0
        double stopLoss1 = ProtectMoney.getStopLoss(TradeType.LONG, 10, 100);
        assertEquals(-200.0, stopLoss1, 0.01);

        // Case 2: Percent stop loss is smaller (should be used)
        // price=10000, lotSize=100 -> percent: 9900.0, absolute: 10000 - 30 = 9970
        // Min should be 9900.0
        double stopLoss2 = ProtectMoney.getStopLoss(TradeType.LONG, 100, 10000);
        assertEquals(9900.0, stopLoss2, 0.01);
    }

    @Test
    @DisplayName("Should use minimum of percent and absolute stop loss for SHORT")
    void testGetStopLossUsesMinimumForShort() {
        // With stopLossPercent = 1% and stopLossAbsolute = 3000
        // For SHORT: percent gives price * 1.01, absolute gives price + (3000/lotSize)
        
        // Case 1: Percent stop loss is smaller (should be used)
        // price=10000, lotSize=100 -> percent: 10100.0, absolute: 10000 + 30 = 10030
        // Min should be 10030.0
        double stopLoss1 = ProtectMoney.getStopLoss(TradeType.SHORT, 100, 10000);
        assertEquals(10030.0, stopLoss1, 0.01);
        
        // Case 2: Absolute stop loss is smaller (should be used)
        // price=100, lotSize=10 -> percent: 101.0, absolute: 100 + 300 = 400
        // Min should be 101.0
        double stopLoss2 = ProtectMoney.getStopLoss(TradeType.SHORT, 10, 100);
        assertEquals(101.0, stopLoss2, 0.01);
    }

    @Test
    @DisplayName("Should throw exception for LONG_EXIT trade type")
    void testGetStopLossThrowsExceptionForLongExit() {
        assertThrows(RuntimeException.class, () -> {
            ProtectMoney.getStopLoss(TradeType.LONG_EXIT, 100, 100);
        });
    }

    @Test
    @DisplayName("Should throw exception for SHORT_EXIT trade type")
    void testGetStopLossThrowsExceptionForShortExit() {
        assertThrows(RuntimeException.class, () -> {
            ProtectMoney.getStopLoss(TradeType.SHORT_EXIT, 100, 100);
        });
    }

    @Test
    @DisplayName("Should return correct PM properties")
    void testGetPmProperties() {
        List<Kvp> properties = ProtectMoney.getPmProperties();
        
        assertNotNull(properties, "Properties list should not be null");
        assertEquals(2, properties.size(), "Should have exactly 2 properties");
        
        // Check stopLossPercent property
        Kvp stopLossPercentProp = properties.getFirst();
        assertEquals("ProtectMoney:StopLossPercent", stopLossPercentProp.key());
        assertEquals("1.0", stopLossPercentProp.value());
        
        // Check stopLossAbsolute property
        Kvp stopLossAbsoluteProp = properties.get(1);
        assertEquals("ProtectMoney:StopLossAbsolute", stopLossAbsoluteProp.key());
        assertEquals("3000.0", stopLossAbsoluteProp.value());
    }

    @Test
    @DisplayName("Should handle edge case with very small price")
    void testGetStopLossWithVerySmallPrice() {
        // price=1.0, lotSize=100 -> percent: 0.99, absolute: 1.0 - 30 = -29.0
        // Min should be -29.0
        double stopLoss = ProtectMoney.getStopLoss(TradeType.LONG, 100, 1.0);
        assertEquals(-29.0, stopLoss, 0.01);
    }

    @Test
    @DisplayName("Should handle edge case with very large price")
    void testGetStopLossWithVeryLargePrice() {
        double stopLoss = ProtectMoney.getStopLoss(TradeType.LONG, 100, 100000.0);
        assertTrue(stopLoss > 0, "Stop loss should be positive for large prices");
        assertEquals(99000.0, stopLoss, 0.01);
    }

    @Test
    @DisplayName("Should handle edge case with lot size of 1")
    void testGetStopLossWithLotSizeOne() {
        // With lotSize=1, absolute stop loss = price - 3000 (for LONG)
        double stopLoss = ProtectMoney.getStopLoss(TradeType.LONG, 1, 5000.0);
        // percent: 4950.0, absolute: 5000 - 3000 = 2000
        // Min should be 2000.0
        assertEquals(2000.0, stopLoss, 0.01);
    }

    @Test
    @DisplayName("Should handle edge case with very large lot size")
    void testGetStopLossWithLargeLotSize() {
        // With lotSize=1000, absolute stop loss = price - 3 (for LONG)
        double stopLoss = ProtectMoney.getStopLoss(TradeType.LONG, 1000, 100.0);
        // percent: 99.0, absolute: 100 - 3 = 97
        // Min should be 97.0
        assertEquals(97.0, stopLoss, 0.01);
    }
}

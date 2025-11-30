package com.vrtading.trademanager;

public final class Utils {
    private Utils() {
        throw new IllegalStateException("Utility class");
    }

    public static TradeDirection getTradeDirection(TradeType tradeType) {
        if (tradeType == TradeType.LONG) {
            return TradeDirection.LONG;
        } else if (tradeType == TradeType.SHORT) {
            return TradeDirection.SHORT;
        }
        throw new IllegalArgumentException("Unexpected TradeType: " + tradeType);
    }
}

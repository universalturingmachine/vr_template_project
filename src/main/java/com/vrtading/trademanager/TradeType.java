package com.vrtading.trademanager;

public enum TradeType {
    LONG, SHORT, LONG_EXIT, SHORT_EXIT;

    public static boolean isEntryTradeType(TradeType tradeType) {
        return tradeType == LONG || tradeType == SHORT;
    }

    public static boolean isExitTradeType(TradeType tradeType) {
        return tradeType == LONG_EXIT || tradeType == SHORT_EXIT;
    }
}

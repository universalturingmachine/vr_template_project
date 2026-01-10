package com.vrtrading.trademanager;

public enum TradeType {
    LONG, SHORT, LONG_EXIT, SHORT_EXIT;

    public boolean isEntryTradeType() {
        return this == LONG || this == SHORT;
    }

    public boolean isExitTradeType() {
        return this == LONG_EXIT || this == SHORT_EXIT;
    }
}

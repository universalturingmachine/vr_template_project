package com.vrtading.trademanager;

import java.time.LocalDate;

public record Instrument(
    long instrumentToken,
    long exchangeToken,
    String tradingSymbol,
    String name,
    double lastPrice,
    double tickSize,
    String instrumentType,
    String segment,
    String exchange,
    String strike,
    int lotSize,
    LocalDate expiry,
    LocalDate cacheDate
) {
}

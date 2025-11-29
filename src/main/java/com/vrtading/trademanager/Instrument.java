package com.vrtading.trademanager;

import com.mytrading.utils.DecimalValue;

import java.time.LocalDate;

public record Instrument(
    long instrumentToken,
    long exchangeToken,
    String tradingSymbol,
    String name,
    DecimalValue lastPrice,
    DecimalValue tickSize,
    String instrumentType,
    String segment,
    String exchange,
    String strike,
    int lotSize,
    LocalDate expiry,
    LocalDate cacheDate
) {
}

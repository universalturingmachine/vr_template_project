package com.vrtading.trademanager;

import com.mytrading.utils.DecimalValue;
import lombok.NonNull;

import java.time.OffsetDateTime;

public record VRTrade(
        String brokerName,
        String instrumentId,
        String exchange,
        String segment,
        OffsetDateTime timestamp,
        String tradeId,
        TradeType type,
        DecimalValue quantity,
        DecimalValue price,
        StopLoss stopLoss
) {
	@NonNull
    @Override
	public String toString() {
		return "VRTrade [" + instrumentId + ", " + timestamp + ", type=" + type + ", quantity=" + quantity
				+ ", price=" + price + ", stopLoss=" + stopLoss + "]";
	}

    public boolean isEntryTrade() {
        return type.isEntryTradeType();
    }
}

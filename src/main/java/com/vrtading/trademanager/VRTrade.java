package com.vrtading.trademanager;

import java.time.OffsetDateTime;

public record VRTrade(
	CoreInstrument coreInstrument,
	OffsetDateTime timestamp,
	int tradeId,
	TradeType type,
	int quantity,
	double avgPrice
) {
	@Override
	public String toString() {
		return "VRTrade [" + coreInstrument.tradingSymbol() + ", " + timestamp + ", type=" + type + ", quantity=" + quantity
				+ ", avgPrice=" + avgPrice + "]";
	}
}

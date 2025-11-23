package com.vrtading.trademanager;

public record CoreInstrument(
	long instrumentToken,
	String tradingSymbol,
	String exchange,
	String segment
) {
	public CoreInstrument(Instrument instrument) {
		this(
			instrument.instrumentToken(),
			instrument.tradingSymbol(),
			instrument.exchange(),
			instrument.segment()
		);
	}
}

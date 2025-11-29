package com.vrtading.trademanager;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TradeGroup {
	private final List<TradeSequence> tradeSequences;

	public TradeGroup() {
		tradeSequences = new ArrayList<> ();
	}

	public void enterTrade(VRTrade vrTrade) {
        if(tradeSequences.isEmpty() || !tradeSequences.getLast().isActive()) {
            TradeSequence tradeSequence = new TradeSequence();
            tradeSequences.add(tradeSequence);
        }
        tradeSequences.getLast().addTrade(vrTrade);
	}
}

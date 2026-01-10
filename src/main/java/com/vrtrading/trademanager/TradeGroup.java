package com.vrtrading.trademanager;

import com.mytrading.utils.Bar;
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
        if(!isActive()) {
            TradeDirection tradeDirection = Utils.getTradeDirection(vrTrade.type());
            TradeSequence tradeSequence = new TradeSequence(tradeDirection);
            tradeSequences.add(tradeSequence);
        }
        tradeSequences.getLast().addTrade(vrTrade);
	}

    public void newBarArrived(Bar bar) {
        if(!isActive()) {
            return;
        }
        tradeSequences.getLast().newBarArrived(bar);
    }

    public TradeSequence getActiveTradeSequence() {
        return isActive() ? tradeSequences.getLast(): null;
    }

    private boolean isActive() {
        return !tradeSequences.isEmpty() && tradeSequences.getLast().isActive();
    }
}

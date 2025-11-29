package com.vrtading.trademanager;

import com.mytrading.utils.DecimalValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TradeSequence {
    private final List<VRTrade> tradeList;
	private final DecimalValue totalOutstandingShares;
	@Getter
    private boolean active;

	public TradeSequence() {
        this.tradeList = new ArrayList<>();
        this.totalOutstandingShares = new DecimalValue(0);
        this.active = true;
	}
	
	public void addTrade(VRTrade vrTrade) {
		if(!active) {
			//TODO raise a better exception
			log.info("**************************");
			log();
			log.info("**************************");
			throw new RuntimeException();
		}
        tradeList.add(vrTrade);

        if(vrTrade.isEntryTrade()) {
            totalOutstandingShares.add(vrTrade.quantity().value());
        }
        else {
            totalOutstandingShares.subtract(vrTrade.quantity().value());

            if(totalOutstandingShares.isZero()) {
                active = false;
            }
        }
	}

	public DecimalValue getTotalOutstandingShares() {
        return totalOutstandingShares.clone();
	}

    public void log() {
		tradeList.forEach((trade)->log.info(trade.toString()));
	}
}

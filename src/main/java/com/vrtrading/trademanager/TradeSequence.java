package com.vrtrading.trademanager;

import com.mytrading.utils.Bar;
import com.mytrading.utils.DecimalValue;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TradeSequence {
    @Getter
    private final TradeDirection tradeDirection;
    private final List<VRTrade> tradeList;
	private final DecimalValue totalOutstandingShares;
	@Getter
    private boolean active;
    @Getter
    private double minPrice;
    @Getter
    private double maxPrice;

	public TradeSequence(TradeDirection tradeDirection) {
        this.tradeDirection = tradeDirection;
        this.tradeList = new ArrayList<>();
        this.totalOutstandingShares = new DecimalValue(0);
        this.active = true;
        this.minPrice = Double.MAX_VALUE;
        this.maxPrice = Double.MIN_VALUE;
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

    public void newBarArrived(Bar bar) {
        if(!active) {
            throw new RuntimeException("Calling newBarArrived on inactive trade sequence");
        }

        minPrice = Double.min(minPrice, bar.low());
        maxPrice = Double.max(maxPrice, bar.high());
    }
}

package com.vrtading.trademanager;

import com.mytrading.utils.Bar;
import com.mytrading.utils.MyUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TradeManager {
	private final Instrument instrument;
	
	private final List<VRTrade> tradeList;
	private final List<TradeGroup> tradeGroups;
	
	private static final int totalLots = 1;

	//TODO store only instrumentCache, instead of storing this instrument cache service.

	public TradeManager(Instrument instrument) {
		this.instrument = instrument;
		tradeList = new ArrayList<> ();
		tradeGroups = new ArrayList<> ();
	}

	public void enterTrade(Bar bar, TradeType tradeType) {
		VRTrade trade = getMyTrade(bar, tradeType);
		
		if(trade == null) {
			return;
		}

        log.debug("adding trade {}", trade);
		tradeList.add(trade);
		
		if(TradeType.isEntryTradeType(tradeType)) {
			double stopLoss = ProtectMoney.getStopLoss(tradeType, instrument.lotSize(), trade.avgPrice());
			tradeGroups.add(new TradeGroup(stopLoss));
		}
		else if(getActiveTradeGroup() == null) {
			
		}
		
		TradeGroup tradeGroup = tradeGroups.getLast();
		tradeGroup.addTrade(trade);
	}
	
	private VRTrade getMyTrade(Bar bar, TradeType tradeType) {
		CoreInstrument coreInstrument = new CoreInstrument(instrument);

		double avgPrice = bar.close();
		OffsetDateTime timestamp = MyUtils.getEndOfAMinute(bar.endTime())
				.atZone(ZoneId.systemDefault())
				.toOffsetDateTime();
		int quantity = instrument.lotSize() * totalLots;

        VRTrade vrTrade = new VRTrade(coreInstrument, timestamp, 0, tradeType, quantity, avgPrice);
		return vrTrade;
	}
	
	public void analyseTrades() {
		tradeGroups.forEach(TradeGroup::log);
        log.info("Total outstanding shares = {}", getTotalOutstandingShares());
	}
	
	public int getTotalOutstandingShares() {
		int size = tradeGroups.size();
		if(size == 0) {
			return 0;
		}
		
		TradeGroup tradeGroup = tradeGroups.get(size-1);
		return tradeGroup.getTotalOutstandingShares();
	}
	
	public int getTotalTrades() {
		return tradeGroups.size();
	}

	public OffsetDateTime getFirstTradeTime() {
		if(tradeList.isEmpty()) {
			return null;
		}

        return tradeList.getFirst().timestamp();
	}
	
	public TradeGroup getActiveTradeGroup() {
		TradeGroup activeTradeGroup = null;
		if(!tradeGroups.isEmpty()) {
			TradeGroup lastTradeGroup = tradeGroups.getLast();
			if(lastTradeGroup.isAlive()) {
				activeTradeGroup = lastTradeGroup;
			}
		}
		return activeTradeGroup;
	}
	
	public boolean isEmpty() {
		return tradeList.isEmpty();
	}
	
	public Instrument getTradingInstrument() {
		return instrument;
	}
}

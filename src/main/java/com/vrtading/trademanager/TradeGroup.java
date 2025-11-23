package com.vrtading.trademanager;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class TradeGroup {
	private final List<VRTrade> entryTradeList;
	private final List<VRTrade> exitTradeList;

	private int activeTradeIndex;
	private int remainingSharesInAti;
	
	private int totalOutstandingShares;
	private final double stopLoss;
	
	private boolean alive = true;
	
	public TradeGroup(double stopLoss) {
		this.entryTradeList = new ArrayList<> ();
		this.exitTradeList = new ArrayList<> ();
		this.stopLoss = stopLoss;
		
		activeTradeIndex = -1;
		remainingSharesInAti = -1;
		
		totalOutstandingShares = 0;
	}
	
	public void addTrade(VRTrade trade) {
		if(!alive) {
			//TODO raise a better exception
			log.info("**************************");
			log();
			log.info("**************************");
			throw new RuntimeException();
		}
		log.info("Entering addTrade");

		TradeType tradeType = trade.type();
		int quantity = trade.quantity();

		if(TradeType.isEntryTradeType(tradeType)) {
			if(entryTradeList.isEmpty()) {
				activeTradeIndex = 0;
				remainingSharesInAti = quantity;
			}

			entryTradeList.add(trade);
			totalOutstandingShares += quantity;
		}
		else {
			if(TradeType.isExitTradeType(tradeType)) {
				alive = false;
			}

			setActiveTradeIndex(trade);
			exitTradeList.add(trade);
			totalOutstandingShares -= quantity;
		}
	}
	
	private void setActiveTradeIndex(VRTrade trade) {
		int remainingQuantity = trade.quantity();

		while(remainingQuantity > 0) {
			if(remainingSharesInAti >= remainingQuantity) {
				remainingSharesInAti -= remainingQuantity;
				remainingQuantity = 0;
			}
			else {
				remainingQuantity -= remainingSharesInAti;
				activeTradeIndex++;
				remainingSharesInAti = entryTradeList.get(activeTradeIndex).quantity();
			}
		}
	}

	public List<VRTrade> getAllTrades() {
		List<VRTrade> allTrades = new ArrayList<> ();
		
		allTrades.addAll(entryTradeList);
		allTrades.addAll(exitTradeList);
		
		allTrades.sort((left, right)->left.timestamp().compareTo(right.timestamp()));
		
		return allTrades;
	}
	
	public int getTotalOutstandingShares() {
		return totalOutstandingShares;
	}

	public double getStopLoss() {
		return stopLoss;
	}
	
	public boolean isAlive() {
		return alive;
	}
	
	public void log() {
		List<VRTrade> allTrades = getAllTrades();
		allTrades.forEach((trade)->log.info(trade.toString()));
	}
}

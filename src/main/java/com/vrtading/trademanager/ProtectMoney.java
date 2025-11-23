package com.vrtading.trademanager;

import com.mytrading.utils.Kvp;
import com.mytrading.utils.MyUtils;

import java.util.List;

public final class ProtectMoney {
    private ProtectMoney() {
        throw new UnsupportedOperationException("It is a utility class and cannot be instantiated");
    }

	private static final double stopLossPercent = 1;
	private static final double stopLossAbsolute = 3000;

	public static double getStopLoss(TradeType tradeType, int lotSize, double price) {
        double stopLossPercent = getStopLossPercent(tradeType, price);
        double stopLossAbsolute = getStopLossAbsolute(tradeType, lotSize, price);

        return Math.min(stopLossPercent, stopLossAbsolute);
	}
	
	private static double getStopLossPercent(TradeType tradeType, double price) {
		int sign = getSign(tradeType);
		double stopLoss = price * (100 + sign * stopLossPercent) / 100; 
		return MyUtils.getRoundedValue(stopLoss);
	}
	
	private static double getStopLossAbsolute(TradeType tradeType, int lotSize, double price) {
		int sign = getSign(tradeType);
		double lossMoney = stopLossAbsolute / lotSize;
		double stopLoss = price + sign * lossMoney;
		return stopLoss;
	}
	
	private static int getSign(TradeType tradeType) {
		int sign = switch (tradeType) {
            case LONG -> -1;
            case SHORT -> 1;
            default ->
                //TODO throw a better exception
                    throw new RuntimeException();
        };

        return sign;
	}
	
	public static List<Kvp> getPmProperties() {
		List<Kvp> pmProps = List.of(
				new Kvp("ProtectMoney:StopLossPercent", String.valueOf(stopLossPercent)),
				new Kvp("ProtectMoney:StopLossAbsolute", String.valueOf(stopLossAbsolute))
				);
		return pmProps;
	}
}

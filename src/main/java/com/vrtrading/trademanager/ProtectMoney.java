package com.vrtrading.trademanager;

import com.mytrading.utils.DecimalValue;
import com.mytrading.utils.Kvp;

import java.util.List;

public final class ProtectMoney {
    private ProtectMoney() {
        throw new UnsupportedOperationException("It is a utility class and cannot be instantiated");
    }

	private static final double stopLossPercent = 1;
	private static final double stopLossAbsolute = 3000;

	public static DecimalValue getStopLoss(TradeType tradeType, int lotSize, DecimalValue price) {
        DecimalValue stopLossPercent = getStopLossPercent(tradeType, price);
        DecimalValue stopLossAbsolute = getStopLossAbsolute(tradeType, lotSize, price);

        return DecimalValue.min(stopLossPercent, stopLossAbsolute);
	}

	private static DecimalValue getStopLossPercent(TradeType tradeType, DecimalValue price) {
		int sign = getSign(tradeType);
		double stopLossValue = price.value() * (100 + sign * stopLossPercent) / 100;
		return new DecimalValue(stopLossValue);
	}

	private static DecimalValue getStopLossAbsolute(TradeType tradeType, int lotSize, DecimalValue price) {
		int sign = getSign(tradeType);
		double lossMoney = stopLossAbsolute / lotSize;
		double stopLossValue = price.value() + sign * lossMoney;
		return new DecimalValue(stopLossValue);
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

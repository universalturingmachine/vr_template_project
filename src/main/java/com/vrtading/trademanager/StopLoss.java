package com.vrtading.trademanager;

import com.mytrading.utils.DecimalValue;
import lombok.NonNull;

public record StopLoss(SLType slType, DecimalValue slValue) {
    public enum SLType {
        Absolute,
        DiffAbsolute,
        DiffPercent
    }

    @NonNull
    @Override
    public String toString() {
        return "StopLoss [slType = " + slType + ", slValue = " + slValue + "]";
    }
}

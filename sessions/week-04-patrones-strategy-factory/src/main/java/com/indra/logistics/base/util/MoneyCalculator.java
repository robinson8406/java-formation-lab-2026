package com.indra.logistics.base.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyCalculator {

    private static final int SCALE = 2;

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
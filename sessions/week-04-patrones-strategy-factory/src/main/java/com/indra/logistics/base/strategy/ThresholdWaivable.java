package com.indra.logistics.base.strategy;

import java.math.BigDecimal;

public interface ThresholdWaivable {

    BigDecimal threshold();
    String waivedMessage();
}
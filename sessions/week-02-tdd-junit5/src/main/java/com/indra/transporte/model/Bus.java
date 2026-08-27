package com.indra.transporte.model;

import com.indra.transporte.model.Enums.BusType;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Bus {
    private final String id;
    private final String plaque;
    private final BusType type;
}

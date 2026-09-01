package com.indra.transporte.model;

import com.indra.transporte.model.Enums.BusType;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Bus {
    private String id;
    private String plaque;
    private BusType type;
    private Route route;
    private Schedule schedule;


    public Bus(String id, String plaque, BusType type) {
        this.id = id;
        this.plaque = plaque;
        this.type = type;
        this.route = null;
        this.schedule = null;
    }
}

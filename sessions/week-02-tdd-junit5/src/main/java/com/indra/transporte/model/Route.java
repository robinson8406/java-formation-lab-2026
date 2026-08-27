package com.indra.transporte.model;

import com.indra.transporte.model.Enums.RouteType;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Route {
    private final String id;
    private final String name;
    private final RouteType type;
}

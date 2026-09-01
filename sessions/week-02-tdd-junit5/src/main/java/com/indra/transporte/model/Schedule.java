package com.indra.transporte.model;

import java.time.LocalTime;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Schedule {
    private final LocalTime arrivalTime;
    private final LocalTime departureTime;

     public boolean overlaps(Schedule other) {
         if(other == null)
             return false;
         return this.arrivalTime.isBefore(other.departureTime)
             && this.departureTime.isAfter(other.arrivalTime);
     }

    public boolean isValidRange(){
        return arrivalTime != null && departureTime != null && arrivalTime.isBefore(departureTime);
    }


}

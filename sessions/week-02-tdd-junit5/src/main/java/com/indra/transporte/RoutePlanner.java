package com.indra.transporte;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.indra.transporte.model.Schedule;
import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Route;
import com.indra.transporte.model.Enums.BusType;
import com.indra.transporte.model.Enums.RouteType;
import lombok.Data;

@Data
public class RoutePlanner {

    private Set<Bus> scheduledBuses = new HashSet<>();
    private Map<String, List<Schedule>> schedulesByBus = new HashMap<>();
    

    public boolean isValidAssignment(Bus bus, Route route) {
        
        boolean isValid = false;

        if(bus == null || route == null)
            return false;

        if(bus.getType() == BusType.Electric){
            isValid=  route.getType() == RouteType.Electric;
        }
        else if(bus.getType() == BusType.Diesel){

            isValid = route.getType() == RouteType.Diesel ||
                    route.getType() == RouteType.Hybrid;
        }
        else if(bus.getType() == BusType.Gas){
            isValid = route.getType() == RouteType.Gas ||
                    route.getType() == RouteType.Hybrid;
        }

        return isValid;
    }

    public void registerBus(Bus bus) {
        if(bus == null)
            throw new IllegalArgumentException("Bus cannot be null");

        if(bus.getType() == null)
            throw new IllegalArgumentException("Bus type cannot be null");

        scheduledBuses.add(bus);
    }

    public void assignSchedule(Bus bus, Route route, Schedule schedule) {

        if(bus == null || route == null || schedule == null)
            throw new IllegalArgumentException("Bus, route, and schedule cannot be null");

        if(!schedule.isValidRange())
            throw new IllegalArgumentException("Invalid schedule range");

        if(!isValidAssignment(bus, route))
            throw new IllegalArgumentException("Invalid bus and route assignment");

        String busId = bus.getId();
        List<Schedule> existingSchedules = schedulesByBus.get(busId);

        if (existingSchedules == null) {
            existingSchedules = new ArrayList<>();
            schedulesByBus.put(busId, existingSchedules);
        }

        for(Schedule existing : existingSchedules) {
            if(schedule.overlaps(existing))
                throw new IllegalArgumentException("Schedule overlaps with an existing schedule for this bus");
        }

        existingSchedules.add(schedule);
    }

    public List<Schedule> getValidSchedules(Bus bus, RouteType routeType) {
        
        if(bus == null || routeType == null)
            throw new IllegalArgumentException("Bus and route type cannot be null");

        Route route = new Route("", "", routeType);

        if(!isValidAssignment(bus, route))
            throw new UnsupportedTypeException("Invalid bus and route type assignment");

        boolean registered = scheduledBuses.stream()
        .anyMatch(scheduledBus -> scheduledBus.getId().equals(bus.getId()));

        if(!registered)
            throw new IllegalArgumentException("Bus is not registered");

        String busId = bus.getId();
        List<Schedule> validSchedules;

        if (schedulesByBus.containsKey(busId)) {
            validSchedules = schedulesByBus.get(busId);
        } else {
            validSchedules = new ArrayList<>();
        }

        return validSchedules;
    }

}

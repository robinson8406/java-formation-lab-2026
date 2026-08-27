package test.java.com.indra.transporte;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.beans.Transient;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.indra.transporte.exception.UnsupportedTypeException;
import com.indra.transporte.model.Bus;
import com.indra.transporte.model.Enums.BusType;
import com.indra.transporte.model.Enums.RouteType;
import com.indra.transporte.model.Route;
import com.indra.transporte.model.Schedule;

public class RouterPlannerTest {

    private Bus electricBus;
    private Bus dieselBus;
    private Route electricRoute;
    private Route dieselRoute;

    @BeforeEach
    void setUp() {
        electricBus = new Bus("1", "ABC123", BusType.Electric);
        dieselBus = new Bus("2", "DEF456", BusType.Diesel);
        electricRoute = new Route("R1", "Electric Route", RouteType.Electric);
        dieselRoute = new Route("R2", "Diesel Route", RouteType.Diesel);
    }

    @Test
    @DisplayName("Router Planner Tests")
    void testElectricBusOnlyOnElectricRoutes() {
        
        assertTrue(programmer.isValidAssignment(electricBus, RouteType.Diesel));
        
    }
    
    @Test
    @DisplayName("Return valid schedules for compatible bus and type")
    void testCheckValidSchedules() {

        programmer.registerBus(electricBus);
        Schedule schedule1 = new Schedule(LocalTime.of(8, 0), LocalTime.of(10, 0));
        Schedule schedule2 = new Schedule(LocalTime.of(10, 0), LocalTime.of(12, 0));

        programmer.assignSchedule(electricBus, electricRoute, schedule1);
        programmer.assignSchedule(electricBus, electricRoute, schedule2);

        List<Schedule> validSchedules = programmer.getValidSchedules(electricBus, RouteType.Electric);
        assertTrue(validSchedules.contains(schedule1));
        assertTrue(validSchedules.contains(schedule2));
    }


    @Test
    @DisplayName("Reject schedule if it falls within another")
    void testRejectScheduleWithinAnother() {
        
        programmer.registerBus(electricBus);
        Schedule schedule1 = new Schedule(LocalTime.of(8, 0), LocalTime.of(10, 0));
        programmer.assignSchedule(electricBus, electricRoute, schedule1);
        Schedule schedule2 = new Schedule(LocalTime.of(9, 0), LocalTime.of(11, 0));

        assertThrows(IllegalArgumentException.class, () -> {
            programmer.assignSchedule(electricBus, electricRoute, schedule2);
        });
    }

    @Test
    @DisplayName("Reject arrival time earlier than departure time")
    void testRejectInvalidRange() {
        
        programmer.registerBus(dieselBus);
        Schedule invalidSchedule = new Schedule(LocalTime.of(10, 0), LocalTime.of(8, 0));

        assertThrows(IllegalArgumentException.class, () -> {
            programmer.assignSchedule(dieselBus, dieselRoute, invalidSchedule);
        });
    }

    @Test
    @DisplayName("Reject null or empty schedule parameters")
    void testRejectNullParameters() {
        
        Schedule schedule = new Schedule(LocalTime.of(8,0), LocalTime.of(10,0));

        assertThrows(IllegalArgumentException.class, () -> {
            programmer.assignSchedule(null, dieselRoute, schedule);
        });
    }

    @Test 
    @DisplayName("Reject invalid bus types")
    void testRejectInvalidBusTypes() {
        
        programmer.registerBus(electricBus);

        assertThrows(UnsupportedTypeException.class, () -> { programmer.getValidSchedules(electricBus, RouteType.Diesel); });

    }

}

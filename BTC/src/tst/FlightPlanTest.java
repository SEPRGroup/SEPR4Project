package tst;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.Before;

import cls.FlightPlan;
import cls.Waypoint;

public class FlightPlanTest {
	FlightPlan flight_plan;
	
	@Before
	public void setUp() {
		flight_plan = new FlightPlan(new Waypoint[]{new Waypoint(0, 0, true), new Waypoint(100, 100, true), new Waypoint(25, 75, false), new Waypoint(75, 25, false), new Waypoint(50, 50, false)}, 
				"Dublin", "Berlin", new Waypoint(100, 100, true), new Waypoint(0, 0, true));
	}
	
	@Test
	public void testGetOriginName() {
		String name = flight_plan.getOriginName();
		assertTrue("Origin name = Dublin", "Dublin" == name);
	}
	
	@Test
	public void testGetDestinationName() {
		String name = flight_plan.getDestinationName();
		assertTrue("Destination name = Berlin", "Berlin" == name);
	}

	@Test
	public void testGetTotalDistance() {		
		double distance = Waypoint.getCostBetween(flight_plan.getRoute()[0], flight_plan.getRoute()[1]);
		assertTrue(flight_plan.getTotalDistance() == (int)distance);
	}
}
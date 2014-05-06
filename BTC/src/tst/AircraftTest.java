package tst;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;

import cls.Aircraft;
import cls.Waypoint;
import cls.Vector;
import cls.Score;

public class AircraftTest {	
	Aircraft test_aircraft;
	Score test_score;
	
	@Before
	public void setUp() {
		Waypoint[] waypoints = new Waypoint[]{new Waypoint(0, 0, true), new Waypoint(100, 100, true), new Waypoint(25, 75, false), new Waypoint(75, 25, false), new Waypoint(50,50, false)};
		test_aircraft = new Aircraft("testAircraft", new Waypoint(100,100, true), new Waypoint(0,0, true), null, 0, 10.0, waypoints, 1);
		test_score = new Score(0, 0);
	}
	
	// Test get functions
	@Test
	public void testGetPosition() {
		Vector resultPosition = test_aircraft.getPosition();
		boolean correctPosition = false;
		// Altitude can be between 10000 and 30000 with an 1000 interval
		for (int i = 10; i<30; i++){
			 
			if ((resultPosition.getZ() == i * 1000) &&
					resultPosition.getY() == 0 &&
					resultPosition.getX() >= -128) {
				 correctPosition = true;
			}
		}		
		
		assertTrue(correctPosition);
	}
	
	@Test
	public void testGetName() {
		String name = test_aircraft.getName();
		assertTrue("Name = testAircraft", "testAircraft" == name);
	}
	
	@Test
	public void testGetIsFinished() {
		boolean status = test_aircraft.isFinished();
		assertFalse("Finished = false", status);
	}
	
	@Test
	public void testIsManuallyControlled() {
		boolean status = test_aircraft.isManuallyControlled();
		assertFalse("Manually controlled = false", status);
	}
	
	@Test
	public void testGetSpeed() {
		double speed = (int) (test_aircraft.getSpeed() + 0.5);
		assertTrue("Speed = 10", speed == 10.0);
	}
	
	@Test
	public void testGetAltitudeState() {
		test_aircraft.setAltitudeState(1);
		int altitude_state = test_aircraft.getAltitudeState();
		assertTrue("Altitude State = 1", altitude_state == 1);
	}
	
	@Test
	public void testIsAt() {
		Waypoint[] waypoints = new Waypoint[]{new Waypoint(0, 0, true), new Waypoint(100, 100, true), 
				new Waypoint(25, 75, false), new Waypoint(75, 25, false), new Waypoint(50,50, false)};
		test_aircraft = new Aircraft("testAircraft", new Waypoint(100,100, true), new Waypoint(0,0, true), null, 10.0, 0, waypoints, 1);
		
		//double x = test_aircraft.getPosition().getX();
		// y = test_aircraft.getPosition().getY();
		//int radius = 16;
		//boolean outOfBounds = (x < radius || x > window.width() + radius - 32 || y < radius || y > window.height() + radius - 176);
		
		assertTrue("Original position = true", test_aircraft.isAt(new Vector(0,0,0)));
	}
	
	// Test set methods
	@Test
	public void testSetAltitudeState() {
		test_aircraft.setAltitudeState(1);
		int altState = test_aircraft.getAltitudeState();
		assertTrue("Altitude State = 1", altState == 1);
	}
	
	@Test		
	public void testIsCloseToEntry() {
		Waypoint[] waypointList = new Waypoint[]{new Waypoint(0, 0, true), new Waypoint(100, 100, true), new Waypoint(25, 75, false), new Waypoint(675, 125, false), new Waypoint(530,520, false)};
		test_aircraft = new Aircraft("testAircraft", new Waypoint(100,100, true), new Waypoint(0,0, true), null, 0, 20.0, waypointList, 1);
		assertTrue(test_aircraft.isCloseToEntry(waypointList[0].getLocation()));			
		assertFalse(test_aircraft.isCloseToEntry(waypointList[1].getLocation()));
		assertFalse(test_aircraft.isCloseToEntry(waypointList[2].getLocation()));
		assertFalse(test_aircraft.isCloseToEntry(waypointList[3].getLocation()));
		assertFalse(test_aircraft.isCloseToEntry(waypointList[4].getLocation()));
	}
}
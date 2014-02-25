package tst;

import static org.junit.Assert.*;

import org.junit.Test;

import cls.Waypoint;

import cls.Vector;

public class WaypointTest {
	// Test Get Functions
	@Test
	public void testGetPosition() {
		Waypoint test_waypoint = new Waypoint(10,10, false);
		Vector result_vector = test_waypoint.getLocation();
		assertTrue("Position = (10, 10, 0)", (10 == result_vector.getX()) && (10 == result_vector.getY()) && (0 == result_vector.getZ()));
	}

	@Test
	public void testIsEntryOrExit() {
		Waypoint test_waypoint = new Waypoint(10,10, false);
		assertTrue("Entry/Exit = false", false == test_waypoint.isEntryAndExit());
	}
	
	@Test
	public void testIsEntryOrExit2() {
		Waypoint test_waypoint = new Waypoint(0, 0, true);
		assertTrue("Entry/Exit = true", true == test_waypoint.isEntryAndExit());
	}
	
	@Test
	public void testIsMouseOver() {
		Waypoint test_waypoint = new Waypoint(5,5, true);
		assertTrue("Mouse over = true", true == test_waypoint.isMouseOver(10,10));
	}
	
	@Test
	public void testIsMouseOver2() {
		Waypoint test_waypoint = new Waypoint(50,50, true);
		assertTrue("Mouse over = false", false == test_waypoint.isMouseOver(10,10));
	}

	@Test
	public void testGetDistanceFrom() {
		Waypoint test_waypoint = new Waypoint(2, 4, false);
		Waypoint test_waypoint_2 = new Waypoint(2, 2, true);
		double result = test_waypoint.getDistanceFrom(test_waypoint_2);
		assertTrue("Cost = 2", 2 == result);
	}
	
	@Test
	public void testGetDistanceFrom2() {
		Waypoint test_waypoint = new Waypoint(6, 15, false);
		Waypoint test_waypoint_2 = new Waypoint(15, 15, true);
		double result = test_waypoint.getDistanceFrom(test_waypoint_2);
		assertTrue("Cost = 9", 9 == result);
	}

	@Test
	public void testGetCostBetween(){
		Waypoint test_waypoint = new Waypoint(2, 4, false);
		Waypoint test_waypoint_2 = new Waypoint(2, 2, true);
		double result = Waypoint.getCostBetween(test_waypoint, test_waypoint_2);
		assertTrue("Cost = 2", 2 == result);
	}
	
	@Test
	public void testGetCostBetween2(){
		Waypoint test_waypoint = new Waypoint(6, 15, false);
		Waypoint test_waypoint_2 = new Waypoint(15, 15, true);
		double result = Waypoint.getCostBetween(test_waypoint, test_waypoint_2);
		assertTrue("Cost = 9", 9 == result);
	}	
}

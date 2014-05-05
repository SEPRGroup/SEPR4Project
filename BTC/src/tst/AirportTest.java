package tst;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import org.lwjgl.LWJGLException;

import cls.Aircraft;
import cls.Airport;
import cls.Waypoint;

@SuppressWarnings("deprecation")

public class AirportTest {
	Airport test_airport;
	Aircraft test_aircraft;
	
	@Before
	public void setUp() {
		
		// Initialise graphics to prevent not loaded images
		try {
			org.lwjgl.opengl.Display.create();
			lib.jog.graphics.initialise();
		} catch (LWJGLException e) {e.printStackTrace();}
		
		test_airport = new Airport("",500,600);
		Waypoint[] waypoint_list = new Waypoint[]{new Waypoint(0, 0, true), new Waypoint(100, 100, true), new Waypoint(25, 75, false), new Waypoint(75, 25, false), new Waypoint(50,50, false)};
		test_aircraft = new Aircraft("testAircraft", new Waypoint(100,100, true), new Waypoint(0,0, true), null, 10.0, waypoint_list, 1);			
	}
	
	/**
	 * The methods in airport isMouseOverArrivals() and isMouseOverDepartures() only call IsMouseInRect() with their relevant parameters
	 * and therefore is ommitted from testing 
	 */
	@Test
	public void testIsWithinRect() {
		int x = 0, y = 0, width = 20, height = 20;
		int test_x = 10, test_y = 10;
		assertTrue("(10, 10) is in the rectangle", test_airport.isWithinRect(test_x, test_y, x, y, width, height));
	
		test_x = 0;
		test_y = 0;
		assertTrue("(0, 0) is in the rectangle", test_airport.isWithinRect(test_x, test_y, x, y, width, height));

		test_x = -10;
		test_y = 0;
		assertFalse("(-10, 0) is not in the rectangle", test_airport.isWithinRect(test_x, test_y, x, y, width, height));

		test_x = 0;
		test_y = -10;
		assertFalse("(0, -10) is not in the rectangle", test_airport.isWithinRect(test_x, test_y, x, y, width, height));

		test_x = 25;
		test_y = 0;
		assertFalse("(25, 0) is in the rectangle", test_airport.isWithinRect(test_x, test_y, x, y, width, height));
		
		test_x = 0;
		test_y = 25;
		assertFalse("(0, 25) is in the rectangle", test_airport.isWithinRect(test_x, test_y, x, y, width, height));
	}
	
	@Test
	public void testAddToHangar() {
		// Dependent on hangar_size 3
		test_airport.addToHangar(test_aircraft, 1);
		assertTrue("The size of the hanger = 1", test_airport.aircraft_hangar.size() == 1);
		
		test_airport.addToHangar(test_aircraft, 1);
		assertTrue("The size of the hanger = 2", test_airport.aircraft_hangar.size() == 2);
		
		test_airport.addToHangar(test_aircraft, 1);
		assertTrue("The size of the hanger = 3", test_airport.aircraft_hangar.size() == 3);
		
		// This should also be 3 because of the maximum size
		test_airport.addToHangar(test_aircraft, 1);
		assertTrue("The size of the hanger = 3", test_airport.aircraft_hangar.size() == 3);
	}
	
	@Test
	public void testSignalTakeOff() {
		test_airport.signalTakeOffTesting();
		assertTrue("The size of the hanger = 0", test_airport.aircraft_hangar.size() == 0);
		
		test_airport.addToHangar(test_aircraft, 1);
		test_airport.signalTakeOffTesting();
		assertTrue("The size of the hanger = 0", test_airport.aircraft_hangar.size() == 0);
	}
	
	@After
	public void tearDown() {
		
		// cannot have more than one window open
		org.lwjgl.opengl.Display.destroy();
	}
}

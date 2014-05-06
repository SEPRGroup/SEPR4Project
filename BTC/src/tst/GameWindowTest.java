package tst;

import static org.junit.Assert.*;

import java.awt.Rectangle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.LWJGLException;

import cls.Aircraft;
import cls.Airport;
import cls.GameWindow;
import cls.Waypoint;

public class GameWindowTest {

	private GameWindow testGame;
	
	@Before
	public void setUp() throws Exception {
		// Initialise graphics to prevent not loaded images
		try {
			org.lwjgl.opengl.Display.create();
			lib.jog.graphics.initialise();
		} catch (LWJGLException e) {e.printStackTrace();}
		
		testGame = new GameWindow(0, 0, 5, 0, 0);
	}

	@After
	public void tearDown() throws Exception {
		// cannot have more than one window open
		org.lwjgl.opengl.Display.destroy();
		
		testGame = null;
	}

	@Test
	public void testGameWindow() {
		Rectangle gameArea = new Rectangle();
		double scale = 5 / 1248.0;
		
		Airport[] airports = new Airport[] {
				new Airport("Mosbear Airport", 600*scale, 200*scale, scale),
				new Airport("Airport", 600*scale,500*scale, scale)
		};
		
		Waypoint[] locationWaypoints = new Waypoint[] { 
				new Waypoint(8, 8, true, "North West Top Leftonia"), // top left
				new Waypoint(8, gameArea.height-8, true, "100 Acre Woods"), // bottom left
				new Waypoint(gameArea.width-8, 8, true, "City of Rightson"), // top right
				new Waypoint(gameArea.width-8, gameArea.height -8, true, "South Sea"), // bottom right
				airports[0], airports[1]
		};	
		Waypoint[] airspaceWaypoints = new Waypoint[] {		
				//All waypoints in the airspace, including location Way Points
				// Airspace waypoints
				new Waypoint(0.10*gameArea.width, 0.07*gameArea.height, false),
				new Waypoint(0.55*gameArea.width, 0.10*gameArea.height, false),
				new Waypoint(0.81*gameArea.width, 0.08*gameArea.height, false),
				new Waypoint(0.39*gameArea.width, 0.21*gameArea.height, false),
				new Waypoint(0.82*gameArea.width, 0.42*gameArea.height, false),
				new Waypoint(0.20*gameArea.width, 0.42*gameArea.height, false),
				new Waypoint(0.16*gameArea.width, 0.66*gameArea.height, false),
				new Waypoint(0.39*gameArea.width, 0.68*gameArea.height, false),
				new Waypoint(0.63*gameArea.width, 0.78*gameArea.height, false),
				new Waypoint(0.78*gameArea.width, 0.78*gameArea.height, false),
				// Destination/origin waypoints - present in this list for pathfinding.
				locationWaypoints[0],
				locationWaypoints[1],
				locationWaypoints[2],
				locationWaypoints[3],
				locationWaypoints[4],
				locationWaypoints[5]
		};
		
		int i = 0;
		
		for (Waypoint wp: testGame.airspaceWaypoints) {
			assertEquals(wp.toString(), testGame.airspaceWaypoints[i].toString());
			i++;
		}
		i = 0;
		for (Waypoint wp: testGame.locationWaypoints) {
			assertEquals(wp.toString(), testGame.locationWaypoints[i].toString());
			i++;
		}
		
		
	}	

	@Test
	public void testTakeOffSequence() {
		Waypoint[] waypoints = new Waypoint[]{new Waypoint(0, 0, true), new Waypoint(100, 100, true), new Waypoint(25, 75, false), new Waypoint(75, 25, false), new Waypoint(50,50, false)};
		Aircraft testAircraft = new Aircraft("testAircraft", new Waypoint(100,100, true), new Waypoint(0,0, true), null, 10.0, 0, waypoints, 1);
		testGame.takeOffSequence(testAircraft);
		
		assertEquals(testAircraft, testGame.getAircraftList().get(0));
	}

	@Test
	public void testGetTime() {
		assertEquals(0.0, testGame.getTime(), 0);		
	}

	@Test
	public void testGetScore() {
		assertEquals(0, testGame.getScore());
	}

	// setControllable exactly the same test
	@Test
	public void testGetControllable() {
		testGame.setControllable(true);
		boolean controllable = testGame.getControllable();
		
		assertTrue("Controllable = true", controllable);
	}
	
	@Test
	public void testIsGameOver() {
		assertFalse("Game is not over", testGame.isGameOver());
	}

	// Also tests getAircraftList() 
	@Test
	public void testGetCrashedAircraft() {
		Boolean empty = testGame.getAircraftList().isEmpty();
		assertTrue("No planes have crashed", empty);
	}

}

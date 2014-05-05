package tst;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import scn.Demo;
import cls.Aircraft;
import cls.GameWindow;
import cls.Score;
import cls.Waypoint;

@SuppressWarnings("deprecation")


public class ScoreTest {
	Aircraft test_aircraft;
	Score test_score;
	
	@Before
	public void setUp() {
		// Initialise graphics to prevent images not loading
		
		try {
			org.lwjgl.opengl.Display.create();
			lib.jog.graphics.initialise();
		} catch (LWJGLException e) {e.printStackTrace();}
		
		Waypoint[] waypoint_list = new Waypoint[]{new Waypoint(0, 0, true), new Waypoint(100, 100, true), new Waypoint(25, 75, false), new Waypoint(75, 25, false), new Waypoint(50,50, false)};
		test_aircraft = new Aircraft("testAircraft", new Waypoint(100,100, true), new Waypoint(0,0, true), null, 10.0, waypoint_list, 1);
		test_score = new Score(0, 0);
	}

	// Test is simplified (assuming difficulty sections had been implemented correctly)
	
	// Testing score feature 

	@Test
	public void testScore() {
		
		
		GameWindow testDemo = new scn.Demo(1).getGame();
		testDemo.getAircraftList().add(test_aircraft);
		Aircraft plane = testDemo.getAircraftList().get(0);

		assertTrue(test_score.getTotalScore() == 0);
		assertTrue(test_score.getMultiplier() == 1);
		assertTrue(test_score.getMultiplierLevel() == 1);
		assertTrue(plane.getBaseScore() == 150);

		// Simulating Demo class' update from here (calling that function would otherwise interfere with testing):

		test_score.increaseTotalScore(test_score.getMultiplier() * plane.getBaseScore());
		while(test_score.getTargetScore() != test_score.getTotalScore()) test_score.update();
			
		assertTrue(test_score.getTotalScore() == 150);
	}

	// Testing multiplier 
	// Tests the multiplier meter will not decrease below 0 at multiplier_level 1
	@Test
	public void testMeterLowerBound() {
		GameWindow testDemo = new Demo(1).getGame();
		testDemo.getAircraftList().add(test_aircraft);
			
		assertTrue(test_score.getMultiplierLevel() == 1);
		assertTrue(test_score.getMultiplier() == 1);
		assertTrue(test_score.getMeterFill() == 0);
			
		test_score.increaseMeterFill(-1);
		while(test_score.getTargetMeterFill() != test_score.getMeterFill()) test_score.update();
			
		assertTrue(test_score.getMultiplierLevel() == 1);
		assertTrue(test_score.getMultiplier() == 1);
		assertTrue(test_score.getMeterFill() == 0);
	}
		
	// Tests maxing the meter_fill (256) to increase multiplier_level
	@Test
	public void testMultiplierLevelIncrease() {
		test_score.resetMultiplier();
		test_score.increaseMeterFill(256);
		while(test_score.getTargetMeterFill() != test_score.getMeterFill()) test_score.update(); // need to set current_meter_fill to target_meter_fill

		assertTrue(test_score.getMultiplierLevel() == 2);
		assertTrue(test_score.getMultiplier() == 3);
		assertTrue(test_score.getMeterFill() == 0);
	}
	
	@Test
	public void testMultiplierLevelDecrease() {
		// Sets meter_fill to 0 at multiplier_level 2
		test_score.resetMultiplier();
		test_score.increaseMeterFill(256);
		
		while(test_score.getTargetMeterFill() != test_score.getMeterFill()) test_score.update();
			
		assertTrue(test_score.getMultiplierLevel() == 2);
		assertTrue(test_score.getMultiplier() == 3);
		assertTrue(test_score.getMeterFill() == 0);
			
		// Tests decreasing the meter beyond it's lower bound and lowering the multiplier_level
		test_score.increaseMeterFill(-1);
		while(test_score.getTargetMeterFill() != test_score.getMeterFill()) test_score.update();
			
		assertTrue(test_score.getMultiplierLevel() == 1);
		assertTrue(test_score.getMultiplier() == 1);
		assertTrue(test_score.getMeterFill() == 255);
	}
		
	// Tests an increase beyond the bound of the meter
	@Test
	public void testLargeMeterFill() {
		test_score.resetMultiplier();
		test_score.increaseMeterFill(513);
		while(test_score.getTargetMeterFill() != test_score.getMeterFill()) test_score.update();
			
		assertTrue(test_score.getMultiplierLevel() == 3);
		assertTrue(test_score.getMultiplier() == 5);
		assertTrue(test_score.getMeterFill() == 1);
	}
			
	// Checks the upper bound of the meter at max multiplier_level
	@Test
	public void testMeterUpperBound() {
		// Initialises the meter/multiplier_level to their max values
		test_score.resetMultiplier();
		test_score.increaseMeterFill(5*256);
		while(test_score.getTargetMeterFill() != test_score.getMeterFill()) test_score.update();
			
		assertTrue(test_score.getMultiplierLevel() == 5);
		assertTrue(test_score.getMultiplier() == 10);
		assertTrue(test_score.getMeterFill() == 256);
	
		// Tests the meter will not increase beyond 256 at multiplier_level 5
		test_score.increaseMeterFill(1);
		while(test_score.getTargetMeterFill() != test_score.getMeterFill()) test_score.update();
			
		assertTrue(test_score.getMultiplierLevel() == 5);
		assertTrue(test_score.getMultiplier() == 10);
		assertTrue(test_score.getMeterFill() == 256);
	}
	
	@After
	public void tearDown() {
		
		// cannot have more than one window open
		org.lwjgl.opengl.Display.destroy();
	}
	
}

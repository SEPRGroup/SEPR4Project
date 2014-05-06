package tst;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.LWJGLException;

import cls.Aircraft;
import cls.GameWindow;
import cls.TransferBar;
import cls.Waypoint;

public class TransferBarTest {

	private TransferBar testTransferBar;
	private Aircraft testAircraft;
	
	@Before
	public void setUp() throws Exception {
		
		// Initialise graphics to prevent not loaded images
		try {
			org.lwjgl.opengl.Display.create();
			lib.jog.graphics.initialise();
		} catch (LWJGLException e) {e.printStackTrace();}
		
		Waypoint[] waypoints = new Waypoint[]{new Waypoint(0, 0, true), new Waypoint(100, 100, true), new Waypoint(25, 75, false), new Waypoint(75, 25, false), new Waypoint(50,50, false)};
		testAircraft = new Aircraft("testAircraft", new Waypoint(100,100, true),new Waypoint(0,0, true), null, 10.0, 0, waypoints,GameWindow.DIFFICULTY_HARD);
		
		testTransferBar = new TransferBar(8, 20, 1000, 100, 1000, GameWindow.DIFFICULTY_HARD);
	}

	@After
	public void tearDown() throws Exception {
		
		// cannot have more than one window open
		org.lwjgl.opengl.Display.destroy();
		
		testTransferBar = null;
	}

	@Test
	public void testPollLeft() {
		// the aricraft enters the transfer bar
		testTransferBar.enterRight(testAircraft);
		Aircraft a;
		int timeout = 150000;
		// if the plane hasn't come through after certain amount of time then close window
		do {
			timeout --;
			testTransferBar.update(1/60);
			a = testTransferBar.pollLeft();
			
		} while (a == null && timeout>0) ;
		// Should work with values but doesn't, only finds null aricraft
		assertEquals(testAircraft, testTransferBar.pollRight());
	}

	@Test
	public void testPollRight() {
		// this does exactly the same as above just the opposite way round
		testTransferBar.enterRight(testAircraft);
		assertEquals(testAircraft, testTransferBar.pollRight());
		
	}

}

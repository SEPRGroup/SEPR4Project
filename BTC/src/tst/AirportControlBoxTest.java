package tst;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.lwjgl.LWJGLException;

import cls.Airport;
import cls.AirportControlBox;
import cls.GameWindow;

public class AirportControlBoxTest {
	private AirportControlBox testAbc;
	@Before
	public void setUp() throws Exception {
		
		// Initialise graphics to prevent not loaded images
		try {
			org.lwjgl.opengl.Display.create();
			lib.jog.graphics.initialise();
		} catch (LWJGLException e) {e.printStackTrace();}
		
		 testAbc = new AirportControlBox(0, 0, 0, 0, new Airport("test", 0, 0), new GameWindow(0, 0, 0, 0, 0));
	}

	@After
	public void tearDown() throws Exception {
		
		// cannot have more than one window open
		org.lwjgl.opengl.Display.destroy();
		testAbc = null;
	}

	
	@Test
	public void testBarProgress() {
		
		// difference < 0				
		assertEquals(0, testAbc.barProgress(20, 10), 0);
		// difference > 5
		assertEquals(1, testAbc.barProgress(10, 16), 0);
		// difference is anything else than the above
		assertEquals(2/5, testAbc.barProgress(2, 1), 0);
		
		
	}
}

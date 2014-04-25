package cls;

import java.awt.Rectangle;
import java.io.File;
import lib.jog.graphics;

public class GameWindow {
			
	/*
	private static int airspace_view_offset_x = 16;
	public static int airspace_view_offset_y = 48;
		=> x, y
	*/
	
	// Position of things drawn to window  
	
	private int x, y, width, height;
	private final Rectangle
		planeInfo, altimeter, airportControl, ordersBox;
	
	// Static Final Ints for difficulty settings
	// Difficulty of demo scene determined by difficulty selection scene
	public final static int DIFFICULTY_EASY = 0;
	public final static int DIFFICULTY_MEDIUM = 1;
	public final static int DIFFICULTY_HARD = 2;
	private final int difficulty;
	
	/** An image to be used for aircraft*/
	private static graphics.Image aircraft_image;
	/** The background to draw in the airspace.*/
	private static graphics.Image background;
	
	
	public static void start(){
		aircraft_image = graphics.newImage("gfx" + File.separator + "plane.png");
		background = graphics.newImage("gfx" + File.separator + "background_base.png");
	}
	
	
	/** Sets up control areas based on x, y, width, height */
	private void setAreas(){
		//precalculate control position increments
		int cSpacing = 8,
			cWidth = width -cSpacing*3,
			cHeight = 112,
			cY = height -(cHeight +cSpacing);
		
		planeInfo.setRect(0, cY, 
				cWidth/4, cHeight );
		altimeter.setRect(cSpacing +cWidth/4, cY, 
				cWidth/5, cHeight );
		airportControl.setRect(cSpacing*2 +(cWidth*9/20), cY, 
				cWidth/5, cHeight );
		ordersBox.setRect(cSpacing*3 +(cWidth*13/20), cY, 
				cWidth*7/20, cHeight );
	}

	
	public GameWindow(int x, int y, int width, int height, int difficulty) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;	
		//set up window controls
		planeInfo = new Rectangle();
		altimeter = new Rectangle();
		airportControl = new Rectangle();
		ordersBox = new Rectangle();
		setAreas();
		
		this.difficulty= difficulty; 	
	}
	


}

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
	
	// Static Final ints for difficulty settings
	public final static int DIFFICULTY_EASY = 0;
	public final static int DIFFICULTY_MEDIUM = 1;
	public final static int DIFFICULTY_HARD = 2;
	private final int difficulty;
	
	/** An image to be used for aircraft*/
	private static graphics.Image aircraftImage;
	/** The background to draw in the airspace.*/
	private static graphics.Image backgroundImage;
	
	private cls.Score score = new cls.Score(); 	
	private boolean shownAircraftWaitingMessage = false;
	private cls.OrdersBox orders;
	
	private double timeElapsed = 0;
	
	/** instance of the airport class*/
	public static Airport airport = new Airport("Mosbear Airport");
	
	
	public static void start(){
		aircraftImage = graphics.newImage("gfx" + File.separator + "plane.png");
		backgroundImage = graphics.newImage("gfx" + File.separator + "background_base.png");
	}
	
	
	public GameWindow(int x, int y, int width, int height, int difficulty) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;	
		this.difficulty= difficulty;
		
		//set up window controls
		planeInfo = new Rectangle();
		altimeter = new Rectangle();
		airportControl = new Rectangle();
		ordersBox = new Rectangle();
		setAreas();
			
	}

	
	public void update(double time_difference){
		timeElapsed += time_difference;
		
		//update score
		score.update();	
		if (airport.getLongestTimeInHangar(timeElapsed) > 5) {
			score.increaseMeterFill(-1);
			if (!shownAircraftWaitingMessage) {
				orders.addOrder(">>> Plane waiting to take off, multiplier decreasing");
				shownAircraftWaitingMessage = true;
			}
		} else {
			shownAircraftWaitingMessage = false;
		}
		
		orders.update(time_difference);
	}
	
	
	public void draw() {
		
	}
	
	
	/** 
	 * Sets up control areas based on x, y, width, height
	 * replaces orders with a OrdersBox of the new size
	 * */
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
		
		orders = new cls.OrdersBox(ordersBox.x +x, ordersBox.y +y, ordersBox.width, ordersBox.height, 6);
	}
	
	
	public double getTime() {
		return timeElapsed;
	}

}

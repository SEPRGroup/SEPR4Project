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
	private double scale;
	private final Rectangle
		scoreArea, gameArea, planeInfo, altimeter, airportControl, ordersBox;
	
	// Static Final ints for difficulty settings
	public final static int DIFFICULTY_EASY = 0;
	public final static int DIFFICULTY_MEDIUM = 1;
	public final static int DIFFICULTY_HARD = 2;
	private final int difficulty;
	
	/** An image to be used for aircraft*/
	private static graphics.Image aircraftImage;
	/** The background to draw in the airspace.*/
	private static graphics.Image backgroundImage;
	
	private cls.Score score;
	private boolean shownAircraftWaitingMessage = false;
	private cls.OrdersBox orders;
	
	private double timeElapsed = 0;
	
	/** instance of the airport class*/
	private Airport airport;
	/** The set of waypoints in the airspace which are origins / destinations */
	public Waypoint[] locationWaypoints;
	
	
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
		scoreArea = new Rectangle();
		gameArea = new Rectangle();
		planeInfo = new Rectangle();
		altimeter = new Rectangle();
		airportControl = new Rectangle();
		ordersBox = new Rectangle();
		setAreas();

		airport = new Airport("Mosbear Airport");
		locationWaypoints= new Waypoint[] { 
				new Waypoint(8, 8, true, "North West Top Leftonia"), // top left
				new Waypoint(8, gameArea.height-8, true, "100 Acre Woods"), // bottom left
				new Waypoint(gameArea.width-8, 8, true, "City of Rightson"), // top right
				new Waypoint(gameArea.width-8, gameArea.height -8, true, "South Sea"), // bottom right
				airport};
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
	 * replaces score with a Score of the new size
	 * replaces orders with a OrdersBox of the new size
	 * */
	private void setAreas(){
		scale = width / 1248.0;
		
		//precalculate score, game, control position increments
		int spacing = 8,
			sHeight = 32,
			gHeight = (int)(scale * 784),
			gY = sHeight +spacing,
			cWidth = width -3*spacing,	//total width available to controls
			cHeight = height -sHeight -gHeight -2*spacing,
			cY = sHeight +gHeight +2*spacing;
		scoreArea.setRect(0, 0,
				width, sHeight);
		gameArea.setRect(0, gY,
				width, gHeight);
		planeInfo.setRect(0, cY, 
				cWidth/4, cHeight );
		altimeter.setRect(spacing +cWidth/4, cY, 
				cWidth/5, cHeight );
		airportControl.setRect(spacing*2 +(cWidth*9/20), cY, 
				cWidth/5, cHeight );
		ordersBox.setRect(spacing*3 +(cWidth*13/20), cY, 
				cWidth*7/20, cHeight );
		
		score = new cls.Score(width, sHeight);
		orders = new cls.OrdersBox(ordersBox.x, ordersBox.y, 
				ordersBox.width, ordersBox.height, 6);
	}
	
	
	public double getTime() {
		return timeElapsed;
	}

}

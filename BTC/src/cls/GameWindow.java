package cls;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lib.RandomNumber;
import lib.jog.graphics;
import lib.jog.input.EventHandler;

public class GameWindow implements EventHandler{
	//Static Final ints for difficulty settings
	public static final int DIFFICULTY_EASY = 0;
	public static final int DIFFICULTY_MEDIUM = 1;
	public static final int DIFFICULTY_HARD = 2;
	
	/** An image to be used for aircraft*/
	private static graphics.Image aircraftImage;
	/** The background to draw in the airspace.*/
	private static graphics.Image backgroundImage;
	
	//Position of things drawn to window  
	private int x, y, width, height;
	private double scale;
	private final Rectangle
		scoreArea, gameArea, planeInfo, altimeterBox, airportControlBox, ordersBox;
	
	private cls.Score score;
	private boolean shownAircraftWaitingMessage = false;
	private cls.Altimeter altimeter;
	private cls.AirportControlBox airportControl;
	private cls.OrdersBox orders;
		
	/** A button to start and end manual control of an aircraft*/
	private lib.ButtonText manualOverrideButton;
	
	/** instance of the airport class*/
	private Airport airport;
	/** The set of waypoints in the airspace which are origins / destinations */
	public Waypoint[] locationWaypoints;
	/** All waypoints in the airspace, INCLUDING locationWaypoints. */
	public Waypoint[] airspaceWaypoints;
	
	private List<Aircraft>
		aircraftInAirspace = new ArrayList<Aircraft>(),
		recentlyDepartedAircraft = new ArrayList<Aircraft>(),
		crashedAircraft = new ArrayList<Aircraft>();
	
	private int difficulty;
	private double timeElapsed = 0;
	private Boolean gameOver = false;
		
	private Aircraft selectedAircraft = null;
	private Waypoint clickedWaypoint= null;
	private int selectedPathPoint = -1; // Selected path point, in an aircraft's route, used for altering the route
	
	/** How long the label will be displayed for in ms */
	private static final int SCORE_LABEL_DISPLAY = 2000;
	

	
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
		altimeterBox = new Rectangle();
		airportControlBox = new Rectangle();
		ordersBox = new Rectangle();
		setAreas();
		{
			int bWidth = 128, bHeight = 64;
			lib.ButtonText.Action manual = new lib.ButtonText.Action() {
				@Override
				public void action() {
					// _selectedAircraft.manuallyControl();
					toggleManualControl();
				}
			};
			manualOverrideButton = new lib.ButtonText(" Take Control", manual,
					(gameArea.width -bWidth)/2, 32, 
					bWidth, bHeight, 
					x +gameArea.x, y +gameArea.y);
		}

		//create waypoints
		airport = new Airport("Mosbear Airport");
		locationWaypoints = new Waypoint[] { 
				new Waypoint(8, 8, true, "North West Top Leftonia"), // top left
				new Waypoint(8, gameArea.height-8, true, "100 Acre Woods"), // bottom left
				new Waypoint(gameArea.width-8, 8, true, "City of Rightson"), // top right
				new Waypoint(gameArea.width-8, gameArea.height -8, true, "South Sea"), // bottom right
				airport
		};	
		airspaceWaypoints = new Waypoint[] {		
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
				locationWaypoints[4]
		};
		
		altimeter.hide();
	}

	
	/**
	 * Update all game objects, ie aircraft, orders box altimeter.
	 * Cause collision detection to occur
	 * Manages recentlyDepartedAircraft
	 */
	public void update(double timeDifference){
		timeElapsed += timeDifference;
		
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
		
		for (Aircraft a : aircraftInAirspace) {
			a.update(timeDifference);
			if (a.isFinished()) {
				a.setAdditionToMultiplier(score.getMultiplierLevel());
				score.increaseMeterFill(a.getAdditionToMultiplier());
				a.setScore(score.calculateAircraftScore(a));
				score.increaseTotalScore(score.getMultiplier() * a.getScore());
				a.setDepartureTime(System.currentTimeMillis());
				recentlyDepartedAircraft.add(a);
		
				if (a.getAdditionToMultiplier() < 0)
					orders.addOrder("<<< The plane has breached separation rules on its path, your multiplier may be reduced ");
				
				switch (RandomNumber.randInclusiveInt(0, 2)){
				case 0:
					orders.addOrder("<<< Thank you Comrade");
					break;
				case 1:
					orders.addOrder("<<< Well done Comrade");
					break;
				case 2:
					orders.addOrder("<<< Many thanks Comrade");
					break;
				}
			}
		}
		checkCollisions(timeDifference);
		
		{	//manage recentlyDepartedAircraft
			double currentTime = System.currentTimeMillis(); // Current (system) time
			ArrayList<Aircraft> aircraftToRemove = new ArrayList<Aircraft>();
			for (Aircraft d : recentlyDepartedAircraft) {
				double 
					departure_time = d.getTimeOfDeparture(), // Time when the plane successfully left airspace 
					leftAirspaceFor = currentTime - departure_time; // How long since the plane left airspace
				if (leftAirspaceFor > SCORE_LABEL_DISPLAY) {
					aircraftToRemove.add(d);
				}				
			}
			recentlyDepartedAircraft.removeAll(aircraftToRemove);
		}

		orders.update(timeDifference);
	}
	

	/** Draw the GUI and all game objects, e.g. aircraft and waypoints */
	public void draw() {
		//System.out.println("set GameWindow");
		graphics.setViewport(x, y, width, height);
		
		{//draw game area
			//System.out.println("set GameWindow.gameArea");
			setViewportRect(gameArea);
			
			//draw background
			graphics.setColour(255, 255, 255, 96);
			graphics.draw(backgroundImage, gameArea.x, 
					gameArea.y, scale);
			
			//{!} DRAW GAME COMPONENTS
			drawPlaneInfo();
			setViewportRect(scoreArea);
			score.draw();
			graphics.setViewport();
			altimeter.draw();
			airportControl.draw();
			orders.draw();		
			
			//draw scoring labels
			drawPlaneScoreLabels();
			
			//draw border
			graphics.setColour(graphics.green);
			graphics.rectangle(false, gameArea.x, gameArea.y,
					gameArea.width, gameArea.height);
			
			//System.out.println("restore GameWindow.gameArea");
			graphics.setViewport();
		}
				
		//System.out.println("restore GameWindow");
		graphics.setViewport();
	}
	
	
	/**
	 * Cause all planes in airspace to update collisions
	 * Catch any resultant game over state, crashed planes
	 * @param time_difference delta time since last collision check
	 */
	private void checkCollisions(double timeDifference) {
		for (Aircraft a : aircraftInAirspace) {
			int collisionState = a.updateCollisions(aircraftInAirspace, score);
			if (collisionState > -1) {
				crashedAircraft.add(a);
				gameOver = true;
			}
		}
		
		if (gameOver){
			aircraftInAirspace.clear();
			airport.clear();
		}
	}
	
	
	/** Causes an aircraft to call methods to handle deselection */
	private void deselectAircraft() {
		if (selectedAircraft != null && selectedAircraft.isManuallyControlled()) {
			selectedAircraft.toggleManualControl();
			manualOverrideButton.setText(" Take Control");
		}
		selectedAircraft = null;
		clickedWaypoint = null; 
		selectedPathPoint = -1;
		altimeter.hide();
	}
	
	
	/** Causes a selected aircraft to call methods to toggle manual control */
	private void toggleManualControl() {
		if (selectedAircraft != null) {
			selectedAircraft.toggleManualControl();
			manualOverrideButton.setText( (selectedAircraft.isManuallyControlled() ? "Remove" : " Take") + " Control");
		}
	}
	
	
	/** Draw the info of a selected plane in the scene GUI */
	private void drawPlaneInfo() {
		graphics.setColour(graphics.green);
		graphics.rectangle(false, planeInfo.x, planeInfo.y, planeInfo.width, planeInfo.height);
		if (selectedAircraft != null) {
			//System.out.println("set Demo.planeInfo");
			graphics.setViewport(planeInfo.x, planeInfo.y, planeInfo.width, planeInfo.height);
			
			graphics.printCentred(selectedAircraft.getName(), 0, 5, 2, planeInfo.width);
			// Altitude
			String altitude = String.format("%.0f", selectedAircraft.getPosition().getZ()) +"£";
			graphics.print("Altitude:", 10, 40);
			//graphics.print(altitude, planeInfo.width -10 -altitude.length()*8, 40);
			graphics.printRight(altitude, planeInfo.width -10, 40, 1, -1);
			// Speed
			String speed = String.format("%.2f", selectedAircraft.getSpeed() * 1.687810) + "$";
			graphics.print("Speed:", 10, 55);
			//graphics.print(speed,  planeInfo.width -10 -speed.length()*8, 55);
			graphics.printRight(speed, planeInfo.width -10, 55, 1, -1);
			// Origin
			String origin = selectedAircraft.getFlightPlan().getOriginName();
			graphics.print("Origin:", 10, 70);
			//graphics.print(origin, planeInfo.width -10 -origin.length()*8, 70);
			graphics.printRight(origin, planeInfo.width -10, 70, 1, -1);
			// Destination
			String destination = selectedAircraft.getFlightPlan().getDestinationName();
			graphics.print("Destination:", 10, 85);
			//graphics.print(destination, planeInfo.width -10 -destination.length()*8, 85);
			graphics.printRight(destination, planeInfo.width -10, 85, 1, -1);
			//System.out.println("restore Demo.planeInfo");
			
			graphics.setViewport();
		}
	}


	/**
	 * Draws points scored for a plane when it successfully leaves the airspace. The points the
	 * plane scored are displayed just above the plane.
	 */
	private void drawPlaneScoreLabels() {
		double currentTime = System.currentTimeMillis(); // Current (system) time
		
		for (Aircraft a : recentlyDepartedAircraft) {
			double 
				departure_time = a.getTimeOfDeparture(), // Time when the plane successfully left airspace 
				leftAirspaceFor = currentTime -departure_time; // How long since the plane left airspace	
			int scoreTextAlpha = (int)( 255 * (SCORE_LABEL_DISPLAY-leftAirspaceFor)/SCORE_LABEL_DISPLAY );
			
			String planeScoreValue = String.valueOf(a.getScore() * score.getMultiplier());
			
			Vector position = a.getFlightPlan().getDestination();
			//constrain label position
			int scoreTextX = (int) position.getX(),
					scoreTextY = (int) position.getY();
			if (scoreTextX < 40) scoreTextX += 50;
			if (scoreTextY < 40) scoreTextY += 50;
			if (scoreTextX > (gameArea.width-240))scoreTextX -= 50;
			if (scoreTextY > (gameArea.width-240)) scoreTextY -= 50;
			
			// Drawing the score
			graphics.setColour(255, 255, 255, scoreTextAlpha);
			graphics.print(planeScoreValue, scoreTextX, scoreTextY, 2);
		}	
	}
	
	
	/**
	 * adds the aircraft passed to the airspace, where it begins its flight plan starting at the airport
	 * @param aircraft
	 */
	public void takeOffSequence(Aircraft aircraft) {
		aircraftInAirspace.add(aircraft);
		// Space to implement some animation features?
	}
	

	private boolean aircraftClicked(int gameX, int gameY) {
		for (Aircraft a : aircraftInAirspace) {
			if (a.isMouseOver(gameX, gameY)) {
				return true;
			}
		}
		return false;
	}
	
	
	private Aircraft findClickedAircraft(int gameX, int gameY) {
		for (Aircraft a : aircraftInAirspace) {
			if (a.isMouseOver(gameX, gameY)) {
				return a;
			}
		}
		return null;
	}
	
	
	private boolean isArrivalsClicked(int gameX, int gameY) {
		return airport.isWithinArrivals(new Vector(gameX,gameY,0))
				&& !airport.is_active;
	}
	
	
	private boolean isDeparturesClicked(int gameX, int gameY) {
		return airport.isWithinDepartures(new Vector(gameX,gameY,0))
				&& !airport.is_active;
	}
	
	
	private boolean compassClicked(int gameX, int gameY) {
		if (selectedAircraft != null) {
			Vector pos = selectedAircraft.getPosition();
			double 
				dx = pos.getX() -gameX ,
				dy = pos.getY() -gameY;
			int r = Aircraft.COMPASS_RADIUS;
			return  dx*dx + dy*dy < r*r;
		}
		else return false;
	}
	
	
	/** wrapper for graphics.setViewport converting from a Rectangle */
	private void setViewportRect(Rectangle rect){
		graphics.setViewport(rect.x, rect.y, rect.width, rect.height);
	}
	
	
	/** 
	 * Sets up control areas based on x, y, width, height
	 * replaces score with a Score of the new size
	 * replaces orders with a OrdersBox of the new size
	 * replaces airportControl with an airportControlBox of the new size
	 * replaces altimeter with an Altimeter of the new size
	 * */
	private void setAreas(){
		scale = width / 1248.0;
		
		//precalculate score, game, control position increments
		int spacing = 8,
			sX = 256 +spacing,
			sWidth = width -sX -spacing -150,
			sHeight = 32,
			gHeight = (int)(scale * 784),
			gY = sHeight +spacing,
			cWidth = width -3*spacing,	//total width available to controls
			cHeight = height -sHeight -gHeight -2*spacing,
			cY = sHeight +gHeight +2*spacing;
		scoreArea.setRect(sX, 0,
				sWidth, sHeight);
		gameArea.setRect(0, gY,
				width, gHeight);
		planeInfo.setRect(0, cY, 
				cWidth/4, cHeight );
		altimeterBox.setRect(spacing +cWidth/4, cY, 
				cWidth/5, cHeight );
		airportControlBox.setRect(spacing*2 +(cWidth*9/20), cY, 
				cWidth/5, cHeight );
		ordersBox.setRect(spacing*3 +(cWidth*13/20), cY, 
				cWidth*7/20, cHeight );
		
		//regenerate sized control components
		score = new cls.Score(scoreArea.width, scoreArea.height);
		airportControl = new cls.AirportControlBox(airportControlBox.x, airportControlBox.y, 
				airportControlBox.width, airportControlBox.height, airport);
		orders = new cls.OrdersBox(ordersBox.x, ordersBox.y, 
				ordersBox.width, ordersBox.height, 6);
		altimeter = new cls.Altimeter(altimeterBox.x, altimeterBox.y,
				altimeterBox.width, altimeterBox.height, orders);
	}
	
	
	/**
	 * Returns array of entry points that are fair to be entry points for a plane (no plane is currently going to exit the airspace there,
	 * also it is not too close to any plane). 
	 */	
	private java.util.List<Waypoint> getAvailableEntryPoints() {
		ArrayList<Waypoint> availableEntryPoints = new ArrayList<Waypoint>();
		
		for (Waypoint w : locationWaypoints) {
			boolean isAvailable = true;
			/* prevents spawning a plane in waypoint both:
			 * -if any plane is currently going towards it 
			 * -if any plane is less than 250 from it */
			
			for (Aircraft a : aircraftInAirspace) {
				// Check if any plane is currently going towards the exit point/chosen originPoint
				// Check if any plane is less than what is defined as too close from the chosen originPoint
				if (a.current_target.equals(w.getLocation()) || a.isCloseToEntry(w.getLocation())) {
					isAvailable = false;
				}	
			}
			
			if (isAvailable){
				availableEntryPoints.add(w);
			}
		}
		return availableEntryPoints;
	}
	
	
	/**
	 * This method provides maximum number of planes using value of multiplier
	 * @return maximum number of planes
	 */
	private int getMaxAircraft() {
		int multiplier = score.getMultiplier();
		switch (multiplier){
		case 1: 
			return 3;
		case 2: 
			return 5;
		default: 
			return multiplier;
		}
	}
	
	
	/** The interval in seconds to generate flights after */
	private int getFlightGenerationInterval() {
		int base = 30;
		/* Planes move faster on higher difficulties so this makes
		them spawn more often to maintain quantity on screen */
		switch (difficulty){
		case DIFFICULTY_EASY:
			return base / getMaxAircraft();
		case DIFFICULTY_MEDIUM:
			return base / (2*getMaxAircraft());
		case DIFFICULTY_HARD:
			return base / (3*getMaxAircraft());
		default:	//error case
			return -1;
		}
	}
	
	
	public double getTime() {
		return timeElapsed;
	}
	
	
	public Boolean isGameOver(){
		return gameOver;
	}

	
	/**
	 * Getter for aircraft list
	 * @return the List of aircraft in the airspace
	 */
	public java.util.List<Aircraft> getAircraftList() {
		return aircraftInAirspace;
	}
	
	/**
	 * Getter for crashed aircraft list
	 * @return the List of aircraft that have crashed while in the airspace
	 */
	public java.util.List<Aircraft> getCrashedAircraft() {
		return crashedAircraft;
	}


	@Override
	public void mousePressed(int key, int x, int y) {
	
	}


	@Override
	public void mouseReleased(int key, int x, int y) {
	
	}


	@Override
	public void keyPressed(int key) {}


	@Override
	public void keyReleased(int key) {

	}
	
}

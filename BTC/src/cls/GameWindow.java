package cls;

import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import lib.RandomNumber;
import lib.jog.graphics;
import lib.jog.input;
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
	scoreArea, gameArea, manualOverrideBox,
	planeInfo, altimeterBox, ordersBox;
	private final Rectangle[]
			airportControlBoxes;

	private cls.Score score;
	private boolean shownAircraftWaitingMessage = false;
	private cls.Altimeter altimeter;
	private cls.AirportControlBox[] airportControls;
	private cls.OrdersBox orders;

	/** A button to start and end manual control of an aircraft*/
	private lib.ButtonText manualOverrideButton;

	/** instance of the airport class*/
	private Airport[] airports;
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
	private final Boolean controllable;
	private Boolean gameOver = false;
	public TransferBuffer[] transfers;	

	private int[] 
			keysLeft = new int[] {input.KEY_LEFT, input.KEY_A},
			keysRight = new int[] {input.KEY_RIGHT, input.KEY_D},
			keysUp = new int[]{input.KEY_W, input.KEY_UP},
			keysDown = new int[]{input.KEY_S, input.KEY_DOWN};

	private Aircraft selectedAircraft = null;
	private Waypoint clickedWaypoint= null;
	private int selectedPathPoint = -1; // Selected path point, in an aircraft's route, used for altering the route
	/** Tracks if manual heading compass of a manually controlled aircraft has been clicked*/
	private boolean compassClicked = false;
	/** Tracks if waypoint of a manually controlled aircraft has been clicked*/
	private boolean waypointClicked = false;
	/** The time elapsed since the last flight was generated*/
	private double timeSinceFlightGeneration = 0;

	/** How long the label will be displayed for in ms */
	private static final int SCORE_LABEL_DISPLAY = 2000;



	public static void start(){
		aircraftImage = graphics.newImage("gfx" +File.separator +"plane.png");
		backgroundImage = graphics.newImage("gfx" +File.separator + "background_base.png");
	}


	public GameWindow(int x, int y, int width, int height, int difficulty,boolean controllable) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;	
		this.difficulty= difficulty;
		this.controllable = controllable;
		
		//set up window control sizings
		scoreArea = new Rectangle();
		gameArea = new Rectangle();
		manualOverrideBox = new Rectangle();
		planeInfo = new Rectangle();
		altimeterBox = new Rectangle();
		airportControlBoxes = new Rectangle[]{
				new Rectangle(), new Rectangle()};
		ordersBox = new Rectangle();
		setAreas();

		//create waypoints
		airports = new Airport[] {
				new Airport("Moscow Airport", 600*scale, 200*scale, scale),
				new Airport("St Petersburg Airport", 600*scale, 500*scale, scale)
		};
		locationWaypoints = new Waypoint[] { 
				new Waypoint(8, 8, true, "North West Top Leftonia"), // top left
				new Waypoint(8, gameArea.height-8, true, "100 Acre Woods"), // bottom left
				new Waypoint(gameArea.width-8, 8, true, "City of Rightson"), // top right
				new Waypoint(gameArea.width-8, gameArea.height -8, true, "South Sea"), // bottom right
				airports[0], airports[1]
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
				locationWaypoints[4],
				locationWaypoints[5]
		};

		//generate sized components
		score = new cls.Score(scoreArea.width, scoreArea.height);

		orders = new cls.OrdersBox(ordersBox.x, ordersBox.y, 
				ordersBox.width, ordersBox.height, 6);
		if (controllable) {
			altimeter = new cls.Altimeter(altimeterBox.x, altimeterBox.y,
					altimeterBox.width, altimeterBox.height, orders);
			altimeter.hide();
			airportControls = new AirportControlBox[2];
			for (int i=0; i<airports.length; i++){
				Rectangle box = airportControlBoxes[i];
				airportControls[i] = new cls.AirportControlBox(box.x, box.y, 
						box.width, box.height, airports[i], this);
			}
		}
		/*{
			lib.ButtonText.Action manual = new lib.ButtonText.Action() {
				@Override
				public void action() {
					// _selectedAircraft.manuallyControl();
					toggleManualControl();
				}
			};
			manualOverrideButton = new lib.ButtonText(" Take Control", manual,
					manualOverrideBox.x, manualOverrideBox.y,
					manualOverrideBox.width, manualOverrideBox.height,
					0, 0);
		}*/

		{	//set up interface
			transfers = new TransferBuffer[locationWaypoints.length];
			for (int i=0; i<locationWaypoints.length; i++)
				transfers[i] = new TransferBuffer(locationWaypoints[i].getName());
		}

	}


	/**
	 * Manage recentlyDepartedAircraft
	 * Update all game objects, ie aircraft, orders box altimeter.
	 * Cause collision detection to occur
	 * Generate a new flight if flight generation interval has been exceeded.
	 */
	public void update(double timeDifference){
		timeElapsed += timeDifference;

		if(controllable){	//manage recentlyDepartedAircraft
		
			double currentTime = System.currentTimeMillis(); // Current (system) time
			ArrayList<Aircraft> aircraftToRemove = new ArrayList<Aircraft>();
			for (Aircraft d : recentlyDepartedAircraft) {
				double 
				departure_time = d.getTimeOfDeparture(), // Time when the plane successfully left airspace 
				leftAirspaceFor = currentTime -departure_time; // How long since the plane left airspace
				if (leftAirspaceFor > SCORE_LABEL_DISPLAY) {
					aircraftToRemove.add(d);
				}				
			}
			recentlyDepartedAircraft.removeAll(aircraftToRemove);
		}

		//handle controlled aircraft
		if (selectedAircraft != null) {
			if(controllable && !selectedAircraft.is_takeoff()){
				Boolean
				turnLeft = input.keyPressed(keysLeft),
				turnRight = input.keyPressed(keysRight);
				if (!selectedAircraft.isManuallyControlled()
						&& (turnLeft || turnRight)){
					toggleManualControl();
				}
				if (selectedAircraft.isManuallyControlled()) {
					if (turnLeft) {
						selectedAircraft.turnLeft(timeDifference);
					} else if (turnRight) {
						selectedAircraft.turnRight(timeDifference);
					}
				}

				if (input.keyPressed(keysDown)) {
					selectedAircraft.setAltitudeState(Aircraft.ALTITUDE_FALL);
				} else if (input.keyPressed(keysUp)) {
					selectedAircraft.setAltitudeState(Aircraft.ALTITUDE_CLIMB);
				}
			}

			if (isOutOfAirspaceBounds(selectedAircraft)) {
				orders.addOrder(">>> " + selectedAircraft.getName() + " out of bounds, returning to route");
				deselectAircraft();
			}	
		}

		//update aircraft in airspace
		for (Aircraft a : aircraftInAirspace)
			a.update(timeDifference);
		if(controllable){
			checkCollisions();

			//check for and handle aircraft that have completed their route
			for (int i = aircraftInAirspace.size()-1; i >= 0; i--) {
				Aircraft a = aircraftInAirspace.get(i);
				if (a.isFinished()) {
					if (a == selectedAircraft) {
						deselectAircraft();
					}
					aircraftInAirspace.remove(i);
					recentlyDepartedAircraft.add(a);
					for (TransferBuffer tb : transfers){
						if (tb.name == a.getFlightPlan().getDestinationName()){
							tb.transferOut(a);
							break;
						}
					}

					a.setAdditionToMultiplier(score.getMultiplierLevel());
					score.increaseMeterFill(a.getAdditionToMultiplier());
					a.setScore(score.calculateAircraftScore(a));
					score.increaseTotalScore(score.getMultiplier() * a.getScore());
					a.setDepartureTime(System.currentTimeMillis());

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


			//update airports
			for (Airport a : airports )
				a.update(aircraftInAirspace);

			{	//handle if Aircraft are waiting to take off
				boolean waiting = false;
				for (Airport a: airports){
					if (a.getLongestTimeInHangar(timeElapsed) > 5) {
						score.increaseMeterFill(-1);	//decrease meter per Airport
						waiting = true;
					}
				}
				if (waiting) {
					if (!shownAircraftWaitingMessage) {
						orders.addOrder(">>> Plane waiting to take off, multiplier decreasing");
						shownAircraftWaitingMessage = true;
					}
				} else {
					shownAircraftWaitingMessage = false;
				}
			}
		}
		checkTransfers();
		if (controllable){	//generate Flights
			timeSinceFlightGeneration += timeDifference;
			int interval = getFlightGenerationInterval();
			if (timeSinceFlightGeneration >= interval) {
				timeSinceFlightGeneration -= interval;
				if (aircraftInAirspace.size() < getMaxAircraft()) {
					generateFlight();
				}
			}
			if (aircraftInAirspace.size() == 0)
				generateFlight();
		}


		//update controls
		score.update();
		
		orders.update(timeDifference);
	}


	/** Draw the GUI and all game objects, e.g. aircraft and waypoints */
	public void draw() {
		//transformed mouse position for coordinate system used for drawing
		int	intX = input.mouseX() -this.x,
				intY = input.mouseY() -this.y,
				gameX = intX -gameArea.x,
				gameY = intY -gameArea.y;

		//System.out.println("set GameWindow");
		graphics.setViewport(x, y, width, height);

		setViewportRect(scoreArea);
		score.draw();
		graphics.setViewport();
		drawAdditional();

		{	//draw game area
			//System.out.println("set GameWindow.gameArea");
			setViewportRect(gameArea);

			drawMap();

			//draw scoring labels
			drawPlaneScoreLabels();

			//draw UI
			if (selectedAircraft != null){
				//Compass
				if (selectedAircraft.isManuallyControlled() && !selectedAircraft.is_takeoff()) {
					int	mouseX = input.mouseX() -x -gameArea.x,
							mouseY = input.mouseY() -y -gameArea.y;
					selectedAircraft.drawCompass(mouseX, mouseY);
				}
				/*// Override Button
				graphics.setColour(graphics.black);
				drawRect(true, manualOverrideBox);
				graphics.setColour(graphics.white);
				drawRect(false, manualOverrideBox);
				manualOverrideButton.draw(gameX, gameY);*/
			}

			//draw border
			graphics.setColour(graphics.white);
			graphics.rectangle(false, 1, 1,
					gameArea.width-1, gameArea.height-1);

			//System.out.println("restore GameWindow.gameArea");
			graphics.setViewport();
		}

		//draw control panels
		if (controllable){
			drawPlaneInfo();
			altimeter.draw(intX, intY);
			for (AirportControlBox ac : airportControls)
				ac.draw(timeElapsed);
		}
		orders.draw();	

		//System.out.println("restore GameWindow");
		graphics.setViewport();
	}


	/**
	 * Cause all planes in airspace to update collisions
	 * Catch any resultant game over state, crashed planes
	 * @param time_difference delta time since last collision check
	 */
	private void checkCollisions() {
		for (Aircraft a : aircraftInAirspace) {
			int collisionState = a.updateCollisions(aircraftInAirspace, score);
			if (collisionState > -1) {
				crashedAircraft.add(a);
				gameOver = true;
			}
		}

		if (gameOver){
			aircraftInAirspace.clear();
			for (Airport a : airports){
				a.clear();
			}
		}
	}


	/** Causes an aircraft to call methods to handle deselection */
	private void deselectAircraft() {
		if (selectedAircraft != null && selectedAircraft.isManuallyControlled()) {
			selectedAircraft.toggleManualControl();
			//manualOverrideButton.setText(" Take Control");
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
			//manualOverrideButton.setText( (selectedAircraft.isManuallyControlled() ? "Remove" : " Take") + " Control");
		}
	}

	private void checkTransfers(){
		for (int i=0; i<transfers.length; i++){
			Aircraft a = transfers[i].pollIn();
			while (a != null) {
				Waypoint origin, destination;
				//find corresponding origin point
				origin = locationWaypoints[i];
				//generate valid destination point
				do {
					destination = locationWaypoints[RandomNumber.randInclusiveInt(0, locationWaypoints.length -1)];
				} while (destination.getLocation().equals(origin.getLocation()));
				//clone Aircraft, generating new FlightPlan
				Aircraft b = new Aircraft(a.getName(), destination, origin, aircraftImage,
						scale, a.getSpeed(), airspaceWaypoints, difficulty);
				a = null;
				b.getPosition().setZ( b.getPosition().getZ() );
				//shunt the aircraft into the airspace
				if (origin instanceof Airport){
					orders.addOrder("<<< " + b.getName() 
							+ " is awaiting take off from " 
							+ origin.getName()
							+ " heading towards " +destination.getName() + ".");
					((Airport)origin).addToHangar(a, timeElapsed);
				} else {
					orders.addOrder("<<< " + b.getName() 
							+ " transferred from " +origin.getName()
							+ " heading towards " +destination.getName() + ".");
					aircraftInAirspace.add(b);
				}
				a = transfers[i].pollIn();
			}
		}
	}


	/**
	 * Draw background, waypoints, aircraft,
	 * and route of a selected aircraft between waypoints
	 * Print waypoint names next to waypoints
	 */
	private void drawMap() {	
		//draw background
		graphics.setColour(255, 255, 255, 255);
		graphics.draw(backgroundImage, 0, 0, scale);

		//draw waypoints excluding airports
		for (Waypoint w : airspaceWaypoints) {
			if ( !(w instanceof Airport) ) {
				w.draw();
			}
		}
		//draw airports
		graphics.setColour(255, 255, 255, 255);
		for (Airport a : airports)
			a.draw(timeElapsed);

		//draw transfer waypoint labels
		graphics.setColour(graphics.white);
		graphics.print(locationWaypoints[0].getName(), locationWaypoints[0].getLocation().getX() + 9, locationWaypoints[0].getLocation().getY() - 6);
		graphics.print(locationWaypoints[1].getName(), locationWaypoints[1].getLocation().getX() + 9, locationWaypoints[1].getLocation().getY() - 6);
		graphics.print(locationWaypoints[2].getName(), locationWaypoints[2].getLocation().getX() - 141, locationWaypoints[2].getLocation().getY() - 6);
		graphics.print(locationWaypoints[3].getName(), locationWaypoints[3].getLocation().getX() - 91, locationWaypoints[3].getLocation().getY() - 6);
		for (int i=4; i < locationWaypoints.length; i++){
			graphics.print(locationWaypoints[i].getName(), locationWaypoints[i].getLocation().getX() - 20, locationWaypoints[i].getLocation().getY() + 25);
		}

		//correct mouse position for coordinate system
		int	mx = input.mouseX() -x -gameArea.x,
				my = input.mouseY() -y -gameArea.y;

		//draw aircraft and highlight flightplan
		for (Aircraft a : aircraftInAirspace) {
			a.draw();
			if (a.isMouseOver(mx, my)) {
				a.drawFlightPath(false);
			}
		}

		if (selectedAircraft != null) {
			// Flight Path
			selectedAircraft.drawFlightPath(true);
			graphics.setColour(graphics.white);

			//draw flightplan of selected flight
			selectedAircraft.drawFlightPath(true);
		}

		if (clickedWaypoint != null && !selectedAircraft.isManuallyControlled()) {
			selectedAircraft.drawModifiedPath(selectedPathPoint, mx, my);
		}

	}


	/** Draw the info of a selected plane in the scene GUI */
	private void drawPlaneInfo() {
		graphics.setColour(graphics.white);
		graphics.rectangle(false, planeInfo.x, planeInfo.y, planeInfo.width, planeInfo.height);
		if (selectedAircraft != null) {
			//System.out.println("set Demo.planeInfo");
			graphics.setViewport(planeInfo.x, planeInfo.y, planeInfo.width, planeInfo.height);

			graphics.printCentred(selectedAircraft.getName(), 0, 5, 2, planeInfo.width);
			// Altitude
			String altitude = String.format("%.0f", selectedAircraft.getPosition().getZ()) +"£";
			graphics.print("Altitude:", 10, 40);
			graphics.printRight(altitude, planeInfo.width -10, 40, 1, -1);
			// Speed
			String speed = String.format("%.2f", selectedAircraft.getSpeed() * 1.687810) + "$";
			graphics.print("Speed:", 10, 55);
			graphics.printRight(speed, planeInfo.width -10, 55, 1, -1);
			// Origin
			String origin = selectedAircraft.getFlightPlan().getOriginName();
			graphics.print("Origin:", 10, 70);
			graphics.printRight(origin, planeInfo.width -10, 70, 1, -1);
			// Destination
			String destination = selectedAircraft.getFlightPlan().getDestinationName();
			graphics.print("Destination:", 10, 85);
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

			Vector position = a.getFlightPlan().getDestination().getLocation();
			//constrain label position
			int	scoreTextX = (int) position.getX(),
					scoreTextY = (int) position.getY();
			if (scoreTextX < 40) scoreTextX += 50;
			if (scoreTextY < 40) scoreTextY += 50;
			if (scoreTextX > (gameArea.width-240)) scoreTextX -= 50;
			if (scoreTextY > (gameArea.height-100)) scoreTextY -= 50;

			// Drawing the score
			graphics.setColour(255, 255, 255, scoreTextAlpha);
			graphics.print(planeScoreValue, scoreTextX, scoreTextY, 2);
		}	
	}


	/** draw a readout of the time the game has been played for & aircraft in the sky. */
	private void drawAdditional() {
		int	hours = (int)(timeElapsed / (60 * 60)),
				minutes = (int)(timeElapsed / 60) % 60;
		double seconds = timeElapsed % 60;
		java.text.DecimalFormat df = new java.text.DecimalFormat("00.00");
		String timePlayed = String.format("%d:%02d:", hours, minutes) + df.format(seconds);

		graphics.setColour(graphics.white);
		graphics.printRight(timePlayed, width, 8, 1, -1);
		int planes = aircraftInAirspace.size();
		graphics.print(String.valueOf(planes) + " plane" + (planes == 1 ? "" : "s") + " in the sky.", 16, 8);
	}


	/**
	 * adds the aircraft passed to the airspace, where it begins its flight plan starting at the airport
	 * @param aircraft
	 */
	public void takeOffSequence(Aircraft aircraft) {
		aircraftInAirspace.add(aircraft);
		// Space to implement some animation features?
	}


	/**
	 * Creates a new aircraft object and introduces it to the airspace.
	 * Also doesn't spawn a plane that is to close to another so there is 
	 * not an instant crash when spawning 
	 */
	private void generateFlight() {
		Aircraft aircraft = createAircraft();

		if (aircraft != null) {
			//check all the aircraft currently in the airspace
			boolean isTooClose = false;
			Vector originPos = aircraft.getFlightPlan().getOrigin().getLocation();
			for (Aircraft a : aircraftInAirspace) {
				Vector pos = a.getPosition();
				// check the distance from the aircraft and the aircraft waiting to be spawned
				double
				dx = pos.getX() -originPos.getX(),
				dy = pos.getY() -originPos.getY(),
				distance = Math.sqrt(dx*dx + dy*dy);

				// if the distance is less than certain amount then aircraft is too close to be spawned
				if (distance <= 100*scale) {
					isTooClose = true;
					break;
				}				
			}			

			if (!isTooClose) { // Continue only if aircraft are not too close
				Waypoint w = aircraft.getFlightPlan().getOrigin();
				if (w instanceof Airport){
					orders.addOrder("<<< " + aircraft.getName() 
							+ " is awaiting take off from " 
							+ aircraft.getFlightPlan().getOriginName()
							+ " heading towards " + aircraft.getFlightPlan().getDestinationName() + ".");
					((Airport) w).addToHangar(aircraft, timeElapsed);
				} else {
					orders.addOrder("<<< " + aircraft.getName() 
							+ " incoming from " + aircraft.getFlightPlan().getOriginName() 
							+ " heading towards " + aircraft.getFlightPlan().getDestinationName() + ".");
					aircraftInAirspace.add(aircraft);
				}
			}

		}
	}


	/**
	 * Handle nitty gritty of aircraft creating
	 * including randomisation of entry, exit, altitude, etc.
	 * @return the created aircraft object
	 */
	private Aircraft createAircraft() {
		// Origin and Destination
		String name;
		Waypoint 
		originPoint, destinationPoint;

		{	//attempt to find a valid origin
			ArrayList<Waypoint> availableOrigins = getAvailableEntryPoints();	
			if (availableOrigins.isEmpty()) {
				//then consider hangars as a spawn point
				for (Airport a : airports){
					if (a.aircraft_hangar.size() < a.getHangarSize()){
						availableOrigins.add(a);
					}
				}
				if (availableOrigins.size() == 0){
					return null; //no space to generate
				}
			} 
			originPoint = availableOrigins.get(RandomNumber.randInclusiveInt(0, availableOrigins.size()-1));
		}

		{	//find valid destination
			do {
				int destination = RandomNumber.randInclusiveInt(0, locationWaypoints.length -1);
				destinationPoint = locationWaypoints[destination];
			} while (destinationPoint.getLocation().equals(originPoint.getLocation()));
		}


		{	// generate Name
			boolean nameIsTaken;
			do {
				nameIsTaken = false;
				name = "Flight " +RandomNumber.randInclusiveInt(100, 999);
				for (Aircraft a : aircraftInAirspace) {
					if (a.getName() == name){
						nameIsTaken = true;
						break;
					}
				}
			} while (nameIsTaken);
		}

		return new Aircraft(name, 
				destinationPoint, originPoint, 
				aircraftImage, scale, RandomNumber.randInclusiveInt(32, 41), 
				airspaceWaypoints, difficulty);
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


	private Airport getArrivalsClicked(int gameX, int gameY) {
		for (Airport a : airports){
			if(a.isWithinArrivals(new Vector(gameX,gameY,0)) && !a.is_active){
				return a;
			}
		}
		return null;
	}


	private Airport getDeparturesClicked(int gameX, int gameY) {
		for (Airport a : airports){
			if(a.isWithinDepartures(new Vector(gameX,gameY,0)) && !a.is_active){
				return a;
			}
		}
		return null;
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

	/** wrapper for graphics.rectangle converting from a Rectangle */
	private void drawRect(Boolean fill, Rectangle rect){
		graphics.rectangle(fill, rect.x, rect.y, rect.width, rect.height);
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
			sX = 192 +spacing,
			sWidth = width -sX -spacing -150,
			sHeight = 32,
			gHeight = (int)(scale * 784),
			gY = sHeight +spacing,
			bWidth = 112,
			bHeight = 32,
			bY = 32,
			piWidth = controllable ? 304 : 0,
			alWidth = controllable ? (width > 920 ? 232 : 128) : 0,
			cWidth = width -(2+airportControlBoxes.length)*spacing -piWidth -alWidth,	//total width available to resizeable controls
			acWidth = controllable ? cWidth / (3+airportControlBoxes.length) : 0,
			orWidth = controllable ? cWidth*3 / (3+airportControlBoxes.length) : width -2*spacing,
			cHeight = height -sHeight -gHeight -2*spacing,
			cY = sHeight +gHeight +2*spacing;
			scoreArea.setRect(sX, 0,
					sWidth, sHeight);
			gameArea.setRect(0, gY,
					width, gHeight);
			manualOverrideBox.setRect((width -bWidth)/2, bY,	//relative to gameArea
					bWidth, bHeight);
			planeInfo.setRect(1, cY,
					piWidth-1, cHeight );
			altimeterBox.setRect(spacing +piWidth, cY, 
					alWidth, cHeight );
		for (int i=0; i<airportControlBoxes.length; i++){
			airportControlBoxes[i].setRect(spacing*2 +piWidth +alWidth +i*(spacing +acWidth), cY,
					acWidth, cHeight);
		}
			ordersBox.setRect(spacing*2 +piWidth +alWidth +airportControlBoxes.length*(spacing +acWidth), cY, 
					orWidth, cHeight );
	}


	/**
	 * Returns array of entry points that are fair to be entry points for a plane (no plane is currently going to exit the airspace there,
	 * also it is not too close to any plane). 
	 */	
	private ArrayList<Waypoint> getAvailableEntryPoints() {
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


	private boolean isOutOfAirspaceBounds(Aircraft a) {
		Vector pos = a.getPosition();
		double x = pos.getX(), y = pos.getY();
		int r = (int)Math.round(Aircraft.RADIUS*scale);
		return (x < r || x > gameArea.width +r || y < r || y > gameArea.height +r);
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


	private boolean manualOverridePressed(int gameX, int gameY) {
		return manualOverrideButton.isMouseOver(gameX, gameY);
	}


	private boolean waypointInFlightplanClicked(int gameX, int gameY, Aircraft a) {
		if (a != null) {
			FlightPlan plan = a.getFlightPlan();
			for (Waypoint w : airspaceWaypoints) {
				if (plan.indexOfWaypoint(w) > -1 && w.isMouseOver(gameX, gameY)){
					return true;
				}
			}
		}
		return false;
	}


	private Waypoint findClickedWaypoint(int gameX, int gameY) {
		for (Waypoint w : airspaceWaypoints) {
			if (w.isMouseOver(gameX, gameY)) {
				return w;
			}
		}
		return null;
	}


	public double getScale(){
		return scale;
	}
	public double getTime() {
		return timeElapsed;
	}


	public int getScore(){
		return score.getTotalScore();
	}

	public Boolean getControllable() {
		return controllable;
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
		// N/A if not controllable; end here 
		if (!controllable) return;

		//transform for coordinate system used for drawing
		int	intX = x -this.x,
				intY = y -this.y,
				gameX = intX -gameArea.x,
				gameY = intY -gameArea.y;

		for (AirportControlBox ac : airportControls){
			ac.mousePressed(key, intX, intY);
		}
		altimeter.mousePressed(key, intX, intY);
		if (key == input.MOUSE_LEFT) {
			if (aircraftClicked(gameX, gameY)) {
				Aircraft clickedAircraft = findClickedAircraft(gameX, gameY);
				deselectAircraft();
				selectedAircraft = clickedAircraft;
				altimeter.show(selectedAircraft);

			} else if (waypointInFlightplanClicked(gameX, gameY, selectedAircraft) && !selectedAircraft.isManuallyControlled()) {
				clickedWaypoint = findClickedWaypoint(gameX, gameY);
				if (clickedWaypoint != null) {
					waypointClicked = true; // Flag to mouseReleased
					selectedPathPoint = selectedAircraft.getFlightPlan().indexOfWaypoint(clickedWaypoint);					
				}
			}

			Airport
			a = getArrivalsClicked(gameX, gameY),
			b = getDeparturesClicked(gameX, gameY);
			if (a != null && selectedAircraft != null) {
				if (selectedAircraft.is_waiting_to_land && selectedAircraft.current_target.equals(a.getLocation())) {
					a.arrivalsTriggered();
					selectedAircraft.land();
					deselectAircraft();
				}
			} else if (b != null) {	//should not be possible to click two
				Aircraft aircraft = b.signalTakeOff();
				if (aircraft != null){
					takeOffSequence(aircraft);
				}
			}
		} else if (key == input.MOUSE_RIGHT) {
			if (aircraftClicked(gameX, gameY)) {
				selectedAircraft = findClickedAircraft(gameX, gameY);
			}
			if (selectedAircraft != null) {
				if (compassClicked(gameX, gameY)) {
					compassClicked = true; // Flag to mouseReleased
					if (!selectedAircraft.isManuallyControlled())
						toggleManualControl();
				} else {
					if (selectedAircraft.isManuallyControlled()) {
						toggleManualControl();
					} else {
						deselectAircraft();					
					}
				}
			}
		}
	}


	@Override
	public void mouseReleased(int key, int x, int y) {
		// N/A if not controllable; end here 
		if (!controllable) return;

		//transform for coordinate system used for drawing
		int	intX = x -this.x,
				intY = y -this.y,
				gameX = intX -gameArea.x,
				gameY = intY -gameArea.y;

		for (Airport a : airports)
			a.releaseTriggered();
		for (AirportControlBox ac : airportControls)
			ac.mouseReleased(key, intX, intY);
		altimeter.mouseReleased(key, intX, intY);

		switch (key){
		case input.MOUSE_LEFT: 
			/*if (manualOverridePressed(gameX, gameY)) {
				manualOverrideButton.act();
			} else*/ if (selectedAircraft != null && waypointClicked) {
				Waypoint newWaypoint = findClickedWaypoint(gameX, gameY);
				if (newWaypoint != null) {
					selectedAircraft.alterPath(selectedPathPoint, newWaypoint);
					orders.addOrder(">>> " + selectedAircraft.getName() + " please alter your course.");
					orders.addOrder("<<< Roger that. Altering course now.");
				}
				selectedPathPoint = -1;
			}
			clickedWaypoint = null; // Fine to set to null now as will have been dealt with
			break;
		case input.MOUSE_RIGHT:
			if (selectedAircraft != null && compassClicked) {
				Vector pos = selectedAircraft.getPosition();
				double 
				dx = gameX -pos.getX(),
				dy = gameY -pos.getY();
				double newBearing = Math.atan2(dy, dx);
				selectedAircraft.setBearing(newBearing);
			}
			break;
		}
	}


	@Override
	public void keyPressed(int key) {}


	@Override
	public void keyReleased(int key) {
		switch (key) {		
		case input.KEY_SPACE :
			toggleManualControl();
			break;

		case input.KEY_LCRTL :	//{!} for debug only
			generateFlight();
			break;

		case input.KEY_F5 :	//{!} for debug only
			crashedAircraft.add(createAircraft());
			crashedAircraft.add(createAircraft());
			aircraftInAirspace.clear();
			for (Airport a : airports)
				a.clear();
			gameOver = true;
			break;
		}
	}


	public class TransferBuffer{
		public final String name;
		private final Queue<Aircraft> 
		in = new LinkedList<Aircraft>(),
		out = new LinkedList<Aircraft>();

		private TransferBuffer(String name){
			this.name = name;
		}

		public void transferIn(Aircraft a){
			in.add(a);
		}

		public Aircraft pollOut(){
			return out.poll();
		}

		private void transferOut(Aircraft a){
			out.add(a);
		}

		private Aircraft pollIn(){
			return in.poll();
		}

		public void clearOut(){
			out.clear();
		}
	}

}

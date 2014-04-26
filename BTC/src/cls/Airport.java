package cls;

import java.io.File;

import scn.Demo;

import lib.jog.graphics;
import lib.jog.input;
import lib.jog.input.EventHandler;
import lib.jog.window;

public class Airport extends Waypoint implements EventHandler {
	// Put the airport in the middle of the airspace
	private double x_location, y_location;

	// All location values are absolute and based on the current version of the airport image.
	private double arrivals_x_location, arrivals_y_location;
	private static double arrivals_width = 105;
	private static double arrivals_height = 52;
	
	private  double runway_end_x_location, runway_end_y_location, 
					departures_x_location, departures_y_location;

	private static double departures_width = 50;
	private static double departures_height = 36;
	
	public boolean is_active = false; // True if there is an aircraft Landing/Taking off
	private boolean is_arrivals_clicked = false;
	private boolean is_departures_clicked = false;
		
	private static graphics.Image airport;
	
	public java.util.ArrayList<Aircraft> aircraft_waiting_to_land = new java.util.ArrayList<Aircraft>();
	/**
	 * Time entered is directly related to the aircraft hangar and stores the time each aircraft entered the hangar
	 * this is used to determine score multiplier decrease if aircraft is in the hangar for too long
	 */
	public java.util.ArrayList<Aircraft> aircraft_hangar = new java.util.ArrayList<Aircraft>();
	public java.util.ArrayList<Double> time_entered = new java.util.ArrayList<Double>();
	private int hangar_size = 3;
	
	public Airport(String name, double x_location, double y_location) {
		super(x_location, y_location, true, name);
		this.x_location = x_location;
		this.y_location = y_location;
		arrivals_x_location = x_location + 90;
		arrivals_y_location = y_location + 83;
		
		runway_end_x_location = x_location + 120;
		runway_end_y_location = y_location - 65;
		
		departures_x_location = x_location + 2;
		departures_y_location = y_location + 50;
		
		if(airport == null){
			loadImage();
		}
	}
	
	public void loadImage() {
		airport = graphics.newImage("gfx" + File.separator + "Airport.png");
	}
	
	@Override
	public void draw() { 
		// Draw the airport image
		graphics.setColour(64, 64, 64, 128);
		graphics.draw(airport, x_location-airport.width()/2, y_location-airport.height()/2);
		
		int green_fine = 128;
		int green_danger = 0;
		int red_fine = 0;
		int red_danger = 128;
		
		// Draw the hangar button if plane is waiting (departing flights)
		if (aircraft_hangar.size() > 0) {
			// Colour fades from green (fine) to red (danger) over 5 seconds as plane is waiting
			int time_waiting = (int)(Demo.getTime() - time_entered.get(0));
			// Assume it hasn't been waiting
			int green_now = green_fine; 
			int red_now = red_fine;
			if (time_waiting > 0) { // Prevent division by 0
				if (time_waiting >= 5) { // Cap at 5 seconds
					green_now = green_danger;
					red_now = red_danger;
				} else {
					// Colour between fine and danger, scaled by time_waiting
					green_now = green_fine - (int)(Math.abs(green_fine-green_danger) * (time_waiting/5.0)); 
					red_now = (int)(Math.abs(red_fine-red_danger) * (time_waiting/5.0));
				}
			}

			// Draw border, draw as filled if clicked
			graphics.setColour(red_now, green_now, 0, 256);
			graphics.rectangle(is_departures_clicked, departures_x_location-airport.width()/2, departures_y_location-airport.height()/2, departures_width, departures_height);

			// Draw box
			graphics.setColour(red_now, green_now, 0, 64);
			graphics.rectangle(true, departures_x_location-airport.width()/2 + 1, departures_y_location-airport.height()/2 + 1, departures_width - 2, departures_height - 2);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraft_hangar.size()), departures_x_location-airport.width()/2 + 23, departures_y_location-airport.height()/2 + 15);
		}
		graphics.setColour(0, 128, 0, 128);
		// Draw the arrivals button if at least one plane is waiting (arriving flights)
		if (aircraft_waiting_to_land.size() > 0) {
			// Draw border, draw as filled if clicked
			graphics.rectangle(is_arrivals_clicked, arrivals_x_location-airport.width()/2, arrivals_y_location-airport.height()/2, arrivals_width, arrivals_height);
			graphics.setColour(128, 128, 0, 64);			
			// Draw box
			graphics.rectangle(true, arrivals_x_location-airport.width()/2 + 1, arrivals_y_location-airport.height()/2 + 1, arrivals_width -2, arrivals_height -2);
			
			// Print number of aircraft waiting
			graphics.setColour(255, 255, 255, 128);
			graphics.print(Integer.toString(aircraft_waiting_to_land.size()), arrivals_x_location-airport.width()/2 + 50, arrivals_y_location-airport.height()/2 + 26);
		}	
		graphics.setColour(255, 255, 255, 128);
		graphics.print(Integer.toString(1), x_location+120, y_location-65);
	}
	
	public double getLongestTimeInHangar(double currentTime) {
		return aircraft_hangar.isEmpty() ? 0 : currentTime-time_entered.get(0);
	}
	
	/**
	 *  Arrivals is the portion of the airport image which is used to issue the land command
	 * @param position is the point to be tested
	 * @return true if point is within the rectangle that defines the arrivals portion of the airport
	 */
	public boolean isWithinArrivals(Vector position) {
		return isWithinRect((int)position.getX(), (int)position.getY(),(int)(arrivals_x_location-airport.width()/2) + Demo.airspace_view_offset_x, (int)(arrivals_y_location-airport.height()/2) + Demo.airspace_view_offset_y, (int)arrivals_width, (int)arrivals_height);
	}
	
	// Used for calculating if an aircraft is within the airspace for landing - offset should not be applied
	public boolean isWithinArrivals(Vector position, boolean apply_offset) {
		return (apply_offset ? isWithinArrivals(position) : isWithinRect((int)position.getX(), (int)position.getY(),(int)(arrivals_x_location-airport.width()/2), (int)(arrivals_y_location-airport.height()/2), (int)arrivals_width, (int)arrivals_height));
	}
	
	/**
	 * Departures is the portion of the airport image which is used to issue the take off command
	 * @param position is the point to be tested
	 * @return true if point is within the rectangle that defines the departures portion of the airport
	 */
	public boolean isWithinDepartures(Vector position) {
		return isWithinRect((int)position.getX(), (int)position.getY(), (int)(departures_x_location-airport.width()/2) + Demo.airspace_view_offset_x, (int)(departures_y_location-airport.height()/2) + Demo.airspace_view_offset_y, (int)departures_width, (int)departures_height);
	}
	
	public boolean isWithinRect(int test_x, int test_y, int x, int y, int width, int height) {
		return x <= test_x && test_x <= x + width && y <= test_y && test_y <= y + height;
	}
	
	/**
	 * Adds aircraft to the back of the hangar and records the time in the time_entered list
	 * will only add the aircraft if the current size is less than the maximum denoted by hangar_size
	 * @param aircraft
	 */
	public void addToHangar(Aircraft aircraft) {
		if (aircraft_hangar.size() < hangar_size) {
			aircraft_hangar.add(aircraft);
			time_entered.add(Demo.getTime());
		}
	}
	
	public void signalTakeOff() {
		if (!aircraft_hangar.isEmpty()) {
			Aircraft aircraft = aircraft_hangar.remove(0);
			time_entered.remove(0);
			aircraft.takeOff();
		}	
	}
	  
	/** 
	 * Decides whether to draw the radius around the airport by checking if any aircraft which are landing are close
	 * @param demo For getting aircraft list
	 */
	public void update(Demo demo) {
		aircraft_waiting_to_land.clear();
		for (Aircraft a : demo.getAircraftList()) {
			if (a.current_target.equals(this.getLocation())) {
				aircraft_waiting_to_land.add(a);
			}
		}
	}
	
	public int getHangarSize() {
		return hangar_size;
	}
	public Vector getRunwayLocation(){
		return new Vector(runway_end_x_location, runway_end_y_location, 0);
	}
	
	public void arrivalsTriggered(){
		is_arrivals_clicked = true;
	}
	
	public void departuresTriggered(){
		is_departures_clicked = true;
	}
	@Deprecated
	public void mousePressed(int key, int x, int y) {
		if (key == input.MOUSE_LEFT) { 
			if (isWithinArrivals(new Vector(x, y, 0))) {
				is_arrivals_clicked = true;
			} else if (isWithinDepartures(new Vector(x, y, 0))) {
				is_departures_clicked = true;
			}
		}
	}

	public void releaseTriggered(){
		is_arrivals_clicked = false;
		is_departures_clicked = false;
	}
	@Deprecated
	public void mouseReleased(int key, int x, int y) {
		is_arrivals_clicked = false;
		is_departures_clicked = false;
	}

	@Override
	public void keyPressed(int key) {
				
	}

	@Override
	public void keyReleased(int key) {
		
	}
	
	// Used in testing avoiding the need to have a demo instance
	@Deprecated
	public void signalTakeOffTesting() {
		if (aircraft_hangar.size() > 0) {
			aircraft_hangar.remove(0);
			time_entered.remove(0);
		}	
	}

	public void clear() {
		aircraft_hangar.clear();
		time_entered.clear();
	}
}

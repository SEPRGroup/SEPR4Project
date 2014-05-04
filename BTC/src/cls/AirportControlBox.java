package cls;

import lib.jog.graphics;
import lib.jog.input;
import lib.jog.input.EventHandler;

public class AirportControlBox implements EventHandler{
	private final Airport airport;
	private final GameWindow game;
	private int number_of_divisions;	
	private double 
		x_position, y_position,
		width, height,
		division_height;	
	private boolean clicked = false;
	
	/**
	 * Constructor for the control box
	 * @param x the x coordinate to draw at
	 * @param y the y coordinate to draw at
	 * @param w the width of the box
	 * @param h the height of the box
	 * @param airport The airport the box controls
	 */
	public AirportControlBox(double x, double y, double w, double h, Airport airport, GameWindow owner) {
		x_position = x;
		y_position = y;
		width = w;
		height = h;
		this.airport = airport;
		game = owner;
		
		number_of_divisions = airport.getHangarSize() + 1;
		division_height = height / number_of_divisions;
	}
	
	/** Draws the box to the screen */
	public void draw(double current_time) {
		drawBoxOutline();
		drawLabels(current_time);
		if (clicked) {
			graphics.setColour(graphics.green);
			graphics.rectangle(true, x_position, y_position +height -division_height, width, division_height);
		}
	}
	
	/**
	 * Draws the rectangle and the aircraft slots of the the box 
	 * (number of divisions is the hangar size of the airport + 1 for the button to signal take off)
	 */
	private void drawBoxOutline() {
		// Outline
		graphics.setColour(graphics.green);
		graphics.rectangle(false, x_position, y_position, width, height);
		
		// Inner lines
		double y = y_position +height -division_height;
		for (int i = 0; i < number_of_divisions; i++) {
			graphics.line(x_position, y, x_position + width, y);
			y -= division_height;
		}
	}
	
	/** Draws the flight names and time bars, as well as the text on the button either "TAKE OFF" or "AIRPORT BUSY"	*/
	private void drawLabels(double current_time) {
		// Draw take off button
		int opacity = (airport.is_active || airport.aircraft_hangar.size() == 0) ? 128 : 256; // Grey out if not clickable
		graphics.setColour(0, 128, 0, opacity);
		double y = y_position +height -division_height;
		if (!airport.is_active) {
			graphics.print("TAKE OFF", x_position + ((width - 70)/2), y + 9);
		} else {
			graphics.print("AIRPORT BUSY", x_position + ((width - 100)/2), y + 9);
		}
		graphics.setColour(graphics.green);
		
		// Draw aircraft in hangar
		double y_position = y + 12;
		double percentage_complete;
		for (int i = 0; i < airport.aircraft_hangar.size(); i++) {
			y_position -= division_height;
			
			graphics.setColour(graphics.green);
			graphics.print(airport.aircraft_hangar.get(i).getName(), x_position + ((width - 70)/2), y_position - 3);
			
			percentage_complete = barProgress(airport.time_entered.get(i), current_time);
			
			if (percentage_complete == 1) {
				graphics.setColour(graphics.red);
			} else {
				graphics.setColour(128, 128, 0);
			}	
			graphics.line(x_position, y_position + 12, x_position + (width * percentage_complete), y_position + 12);		
		}
		
	}
	
	/**
	 * Returns a ratio of time waiting to 5 (maximum time allowed to wait before punishment), capped at 0 and 1
	 * @param time_entered
	 * @return a value between 0 and 1 which is used to calculate the ratio of the "progress bar" to draw
	 */
	public double barProgress(double time_entered, double current_time) {
		double time_elapsed = current_time -time_entered;
		if (time_elapsed > 5) {
			return 1;
		} else if (time_elapsed < 0) {
			return 0;
		} else {
			return time_elapsed/5;
		}
	}
	
	/**
	 * Returns True if the mouse is over the take off button
	 * @param x Cursor's x Coordinate
	 * @param y Cursor's y Coordinate
	 * @return
	 */
	private boolean isMouseOverTakeOffButton(int x, int y) {
		if (x < x_position || x > x_position + width) return false; 
		if (y < (y_position + height) - (height/number_of_divisions) || y > (y_position + height)) return false;
		return true;		
	}
			
	@Override
	public void mousePressed(int key, int x, int y) {
		if (key == input.MOUSE_LEFT && isMouseOverTakeOffButton(x, y) && airport.aircraft_hangar.size() > 0) {
			clicked = true;
		}
	}


	@Override
	public void mouseReleased(int key, int x, int y) {
		clicked = false;
		if (key == input.MOUSE_LEFT && isMouseOverTakeOffButton(x, y)) {
			if (!airport.is_active) {
				Aircraft a = airport.signalTakeOff();
				if (a != null){
					game.takeOffSequence(a);
				}
			}
		}
	}


	@Override
	public void keyPressed(int key) {}

	@Override
	public void keyReleased(int key) {}
}

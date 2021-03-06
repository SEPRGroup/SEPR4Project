package cls;

import org.newdawn.slick.Color;

import lib.jog.graphics;
import lib.jog.input;
import lib.jog.input.EventHandler;

/**
 * Shows the planes height in feet (whatever units you want). And the current banking of the plane.
 * @author Huw Taylor
 */
public class Altimeter implements EventHandler {
	
	private static final int 
		NONE = 0, TOP = 1, BOTTOM = 2;
	
	private double 
		positionX, positionY,
		width, height;
	private boolean isVisible; // Whether or not the Altimeter should be drawn
	
	private cls.Aircraft currentAircraft; // The current aircraft associated with the altimeter
	private cls.OrdersBox ordersBox;

	private int mouseDownState = NONE;
	
	
	/**
	 * Constructor for the altimeter
	 * @param x the x coord to draw at
	 * @param y the y coord to draw at
	 * @param w the width of the altimeter
	 * @param h the height of the altimeter
	 * @param ob the orders box
	 */
	public Altimeter(double x, double y, double w, double h, cls.OrdersBox ob) {
		positionX = x;
		positionY = y;
		width = w;
		height = h;
		this.ordersBox = ob;
		hide();
	}
	

	/**
	 * Makes the altimeter visible
	 * @param aircraft The aircraft to associate with the altimeter
	 */
	public void show(cls.Aircraft aircraft) {
		if (aircraft != null) {
			currentAircraft = aircraft;
			isVisible = true;
			mouseDownState = NONE;
		}
	}
	
	/** Makes the altimeter invisible */
	public void hide() {
		currentAircraft = null;
		isVisible = false;
	}
	
	/**
	 * Checks if the mouse is over the altimeter
	 * @param mx the x coord of the mouse location
	 * @param my the y coord of the mouse location
	 * @return boolean marking if the mouse is over the altimeter
	 */
	public boolean isMouseOver(int mx, int my) {
		return (mx >= positionX && mx <= positionX + width && my >= positionY && my <= positionY + height);
	}
	
	@Override
	/** Handler for mouse clicks */
	public void mousePressed(int key, int x, int y) {
		if (!isVisible) return;
		mouseDownState = getMouseState(x, y);
	}

	@Override
	/** Handler for mouse releases */
	public void mouseReleased(int key, int mx, int my) {
		if (!isVisible) return;
		if (key == input.MOUSE_LEFT) {
			int mouseUpState = getMouseState(mx, my);
			
			if (mouseUpState == mouseDownState){
				switch (mouseUpState){
				case TOP:
					if (currentAircraft.getAltitudeState() != Aircraft.ALTITUDE_CLIMB)
						currentAircraft.setAltitudeState(Aircraft.ALTITUDE_CLIMB);
					break;
				case BOTTOM:
					if (currentAircraft.getAltitudeState() != Aircraft.ALTITUDE_FALL)
						currentAircraft.setAltitudeState(Aircraft.ALTITUDE_FALL);
					break;
				case NONE:
					//nothing, and skip orders printing
					return;
				}
				mouseDownState = NONE;	//clear mouse state
				ordersBox.addOrder(">>> " + currentAircraft.getName() + ", please adjust your altitude.");
				ordersBox.addOrder("<<< Roger that. Altering altitude now.");
			}
		}
	}

	@Override
	public void keyPressed(int key) { }

	@Override
	public void keyReleased(int key) { }
		

	/** Draws the altimeter to the screen */
	public void draw(int mouseX, int mouseY) {
		drawOutline();
		if (isVisible) {
			drawPlaneIcon();
			drawAltitudes();
			drawAltitudeArrows(mouseX, mouseY);
		}
	}
	
	/** Draws the box around the altimeter */
	private void drawOutline() {
		graphics.setColour(graphics.white);
		graphics.rectangle(false, positionX, positionY, width, height);
	}
	
	/**
	 * Draws the icon on the altimeter
	 * Icon depicts plane orientation
	 */
	private void drawPlaneIcon() {
		// Angle to draw plane
		double r = 0;
		if (currentAircraft.isTurningLeft()) {
			r = -Math.PI / 12;
		} else if (currentAircraft.isTurningRight()) {
			r = Math.PI / 12;
		}
		double x = positionX + (width / 2);
		double y = positionY + (height / 2);
		double wingLength = width / 3 - 8;
		double tailLength = width / 9;
		graphics.line(x, y, x + wingLength * Math.cos(r), y + wingLength * Math.sin(r));
		r -= Math.PI / 2;
		graphics.line(x, y, x + tailLength * Math.cos(r), y + tailLength * Math.sin(r));
		r -= Math.PI / 2;
		graphics.line(x, y, x + wingLength * Math.cos(r), y + wingLength * Math.sin(r));
		graphics.setColour(graphics.black);
		graphics.circle(true, x, y, 4);
		graphics.setColour(graphics.white);
		graphics.circle(false, x, y, 4);
		graphics.printCentred(String.format("%.0f", currentAircraft.getPosition().getZ()), positionX, y+32, 1, width);
	}
	
	/**
	 * Draws the altitude lines relative to the aircraft, showing whether the aircraft is climbing or falling
	 */
	private void drawAltitudes() {
		graphics.setColour(0, 128, 0, 32);
		//System.out.println("set Altimeter");
		graphics.setViewport((int)positionX, (int)positionY, (int)width, (int)height);
		int midX = (int)(width / 2);
		int midY = (int)(height / 2);
		for (int i = -5; i <= 4; i ++) {
			int alt = (int)(currentAircraft.getPosition().getZ() + (1000 * i));
			int offset = (int)( 16.0 * (alt % 1000) / 1000 );
			int y = midY - (i * 16) + offset;
			graphics.line(midX - 64, y, midX + 64, y);
			alt -= (alt % 1000);
			graphics.print(String.valueOf(alt), midX + 72, y);
			graphics.print(String.valueOf(alt), midX - 72 - 40, y);
		}
		//System.out.println("restore Altimeter");
		graphics.setViewport();
		graphics.setColour(graphics.white);
	}
	
	/**
	 * Draws the altitude arrow buttons, used for changing altitude
	 */
	private void drawAltitudeArrows(int mouseX, int mouseY) {
		int mouseState = getMouseState(mouseX, mouseY);
		int midX = (int)( positionX + (width / 2) );
		Color 
			defaultColor = graphics.green,
			selectedColor = (mouseState == mouseDownState) ? graphics.white : graphics.green_transp;

		graphics.setColour( (mouseState == TOP) ? selectedColor : defaultColor);
		graphics.triangle(true, midX - 10, positionY + 10, midX, positionY + 4, midX + 10, positionY + 10);
		
		graphics.setColour((mouseState == BOTTOM) ? selectedColor : defaultColor);
		graphics.triangle(true, midX - 10, positionY + height - 10, midX, positionY + height - 4, midX + 10, positionY + height - 10);
	}
	
	private boolean mouseOverTopArrow(int mx, int my) {
		if (!isVisible) return false;
		if (mx < positionX || mx > positionX + width) return false;
		if (my < positionY || my > positionY + height) return false;
		return (my <= positionY + 16);
	}
	
	private boolean mouseOverBottomArrow(int mx, int my) {
		if (!isVisible) return false;
		if (mx < positionX || mx > positionX + width) return false;
		if (my < positionY || my > positionY + height) return false;
		return (my >= positionY + height - 16);
	}
	
	
	private int getMouseState(int mouseX, int mouseY){
		if (mouseOverTopArrow(mouseX, mouseY))
			return TOP;
		else if (mouseOverBottomArrow(mouseX, mouseY))
			return BOTTOM;
		else 
			return NONE; 
	}
	
}

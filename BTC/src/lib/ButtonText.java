package lib;

import lib.jog.graphics;
import lib.jog.input;

public class ButtonText {
	
	public interface Action {
		public void action();
	}

	private int x, y, width, height, ox, oy;
	private String text;
	private org.newdawn.slick.Color default_colour, hover_colour, unavailable_colour;
	private Action action;
	private boolean is_available;
	
	//#What is ox/oy
	public ButtonText(String text, Action action, int x, int y, int w, int h, int ox, int oy) {
		this.text = text;
		this.action = action;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		this.ox = ox; // x position to print text
		this.oy = oy; // y position to print text
		default_colour = new org.newdawn.slick.Color(0, 128, 0);
		hover_colour = new org.newdawn.slick.Color(128, 128, 128);
		unavailable_colour = new org.newdawn.slick.Color(64, 64, 64);
		is_available = true;
	}
	
	public ButtonText(String text, Action action, int x, int y, int w, int h) {
		this.text = text;
		this.action = action;
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		this.ox = (w - (text.length() * 8)) / 2;
		this.oy = (h - 8) / 2;
		default_colour = new org.newdawn.slick.Color(0, 128, 0);
		hover_colour = new org.newdawn.slick.Color(128, 128, 128);
		unavailable_colour = new org.newdawn.slick.Color(64, 64, 64);
		is_available = true;
	}
	
	public boolean isMouseOver(int mx, int my) {
		return (mx >= x && mx <= x + width && my >= y && my <= y + height);
	}
	
	@Deprecated
	public boolean isMouseOver() { 
		return isMouseOver(input.mouseX(), input.mouseY()); 
	}

	public void setText(String newText) {
		text = newText;
	}
	
	/**
	 * Sets the button text to available - Changing the color to the one specified in ButtonText()
	 * @param is_available - value of the availability, either True or False
	 */
	public void setAvailability(boolean is_available) {
		this.is_available = is_available;
	}
	
	public void act() {
		if (is_available)
			action.action();
	}
	
	@Deprecated
	public void draw() {
		if (!is_available) {
			graphics.setColour(unavailable_colour);
		}
		else if (isMouseOver()) {
			graphics.setColour(hover_colour);
		} else {
			graphics.setColour(default_colour);
		}
		graphics.print(text, x + ox, y + oy);
	}
	
	public void draw(int mouseX, int mouseY) {
		if (!is_available) {
			graphics.setColour(unavailable_colour);
		}
		else if (isMouseOver(mouseX, mouseY)) {
			graphics.setColour(hover_colour);
		} else {
			graphics.setColour(default_colour);
		}
		graphics.print(text, x + ox, y + oy);
	}

}

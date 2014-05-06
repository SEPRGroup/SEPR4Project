package lib;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Input;

import lib.ButtonText.Action;
import lib.jog.graphics;
import lib.jog.input.EventHandler;

public class TextField implements EventHandler{
	private int x, y, width, height,size,maxLength;
	private String text = "";
	private boolean isActive;
	
	
	public TextField( int x, int y, int w, int h,int size) {
		
		this.x = x;
		this.y = y;
		width = w;
		height = h;
		maxLength =  w/(size*8);
		this.size = size;
		isActive = false;
		
	}
	public String getText(){
		return text;
	}
	
	public void draw() {
		
		graphics.setColour(graphics.white);
		graphics.rectangle(true, x, y, width, height);
		graphics.setColour(graphics.black);
		
		graphics.print(text, x , y + (height/2) - size*4,size);
		if(text.length() ==  maxLength){
			return;
		}
		if(isActive){
			if(System.currentTimeMillis()/1000 % 2 == 0) {
				graphics.setColour(graphics.black);
				graphics.print("_",x+text.length() * 8 *size , y+height/2 - size*4,size);
			}
		}
	}
	@Override
	public void mousePressed(int key, int x, int y) {
		
	}
	@Override
	public void mouseReleased(int key, int x, int y) {
		// TODO Auto-generated method stub
		if(x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height){
			isActive = true;
		} else{
			isActive = false;
		}
	}
	@Override
	public void keyPressed(int key) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyReleased(int key) {
		// TODO Auto-generated method stub
		if(isActive){
			 if(Input.getKeyName(key).equals("BACK")){
				if(!(text.length() == 0)){
					text = text.substring(0, text.length() - 1);
				}
				return;
			 }
			if(text.length() >= maxLength){
				return;
			}
			if(Input.getKeyName(key).equals("SPACE")){
				text = text + " ";
			}else if(Input.getKeyName(key).equals("PERIOD")){
				text = text + ".";
			}else if(Input.getKeyName(key).equals("COMMA")){
					text = text + ",";
			}else if(Input.getKeyName(key).equals("SLASH")){
				text = text + "/";
			}else if(Input.getKeyName(key).equals("BACKSLASH")){
				text = text + "\\";
			}
			else{
				
				text = text + Input.getKeyName(key);
			}
			
		}
	}
}

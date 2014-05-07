package cls;

import java.io.File;

import lib.jog.graphics;
import lib.jog.graphics.Image;

public class ScoreIndicator {
	
	private static Image
		background, arrow;
	
	private double 
		x, y, height;
	private double scale;
	private double scoreBalance;
	private int norm1, norm2;

	
	public ScoreIndicator(double x, double y, double height) {
		if (background == null){
			background = graphics.newImage("gfx/new/pointsBackground.png");
			arrow = graphics.newImage("gfx/new/pointsArrow.png");
		}
		this.x = x;
		this.y = y;
		this.height = height;
		this.scale = height*2 / background.width();
	}
	

	public void setScores(int scoreLeft, int scoreRight, int maxScore){
		//set scoreBalance to the representation of 
		int scoreDeficit = maxScore -(scoreLeft +scoreRight);
		norm1 = scoreLeft +scoreDeficit/2;
		norm2 = scoreRight +scoreDeficit/2;
		
		scoreBalance = (scoreRight -scoreLeft)/(double)maxScore;
	}
	
	
	public void draw(){
		double
			oxb = background.width()/2,
			oyb = background.height()/2;
		graphics.setColour(255,255,255);
		graphics.draw(background, scale, x, y, 0, oxb, oyb);
		graphics.draw(arrow, scale, x, y, scoreBalance*Math.PI/2, arrow.width()/2, arrow.height()/2);
		
		graphics.setColour(graphics.red);
		graphics.rectangle(false,x-1, y-1, 2, 2);
	}
	
	

}

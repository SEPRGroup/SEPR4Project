package lib;

import lib.jog.graphics;
import lib.jog.graphics.Image;
import lib.jog.graphics.Quad;

public class SpriteAnimation {

	private Image image; // The animation sequence
	private Quad[] quads; // The rectangle to be drawn from the animation bitmap
	private int number_of_frames; // Number of frames in animation
	private int current_frame_number; // The current frame
	private double time_per_frame; // Milliseconds between each frame (1000/fps)

	private double sprite_width; // The width of the sprite to calculate the cut out rectangle
	private double sprite_height; // The height of the sprite
	
	private boolean has_finished; // Flag which is set to mark when all frames have been drawn

	private int x; // X coordinate of the object (top left of the image)
	private int y; // Y coordinate of the object (top left of the image)
	private double animation_timer; // Tracks how long a frame has been shown for. Updated by the parent scene update(dt)
	private double image_width, image_height;
	private boolean is_looping;
	
	/**
	 * <h1>Sprite Animation</h1>
	 * <p>Creates an animation class</p>
	 * @author Stephen Webb
	 * @param image image from which the quads are taken.
	 * @param x the x position to draw the animation.
	 * @param y the y position to draw the animation.
	 * @param fps how many animation frames to draw per second.
	 * @param frame_count how many frames the animation comprises.
	 * @param frames_wide how many frames wide the image is
	 * @param frames_high how many frames high the image is
	 * @param should_loop whether to loop the animation
	 */
	public SpriteAnimation(Image image, int x, int y, int fps, int frame_count, int frames_wide, int frames_high, boolean should_loop) {
		this.image = image;
		image_width = image.width();
		System.out.println("-----");
		image_height = image.height();
		System.out.println("-----");
		this.x = x;
		this.y = y;
		this.number_of_frames = frame_count;
		current_frame_number = 0;
		sprite_width = image_width / frames_wide;
		sprite_height = image_height / frames_high;
		System.out.println("Image Dimensions: " + image_width + ", " + image_height);
		System.out.println("Frame Dimensions: " + sprite_width + ", " + sprite_height);
		time_per_frame = 1.0/fps;
		animation_timer = 0;
		quads = new Quad[frame_count];
		for (int n = 0; n < frame_count; n ++) {
			int i = n % frames_wide;
			int j = n / frames_wide;
			quads[n] = graphics.newQuad(i * sprite_width, j * sprite_height, sprite_width, sprite_height, image_width, image_height);
		}
		is_looping = should_loop;
		has_finished = false;
	}
	
	/**
	 * <h1>Sprite Animation</h1>
	 * <p>Creates an animation class</p>
	 * @author Stephen Webb
	 * @param image_filepath the filepath to the image.  
	 * @param x the x position to draw the animation.
	 * @param y the y position to draw the animation.
	 * @param fps how many animation frames to draw per second.
	 * @param frame_count how many frames the animation comprises.
	 * @param frames_wide how many frames wide the image is
	 * @param frames_high how many frames high the image is
	 * @param should_loop whether to loop the animation
	 */
	public SpriteAnimation(String image_filepath, int x, int y, int fps, int frame_count, int frames_wide, int frames_high, boolean should_loop) {
		new SpriteAnimation(graphics.newImage(image_filepath), x, y, fps, frame_count, frames_wide, frames_high, should_loop);
	}
	
	/**
	 * Updates the timer and changes the frame if necessary
	 * @param time_difference time in seconds since last update
	 */
	public void update(double time_difference) {
		if (!has_finished) {		
			animation_timer += time_difference;
			if (animation_timer > time_per_frame) { // Frame period exceeded
				animation_timer = 0; // Reset timer
				System.out.print(current_frame_number + ", ");
				current_frame_number++;
				if (current_frame_number >= number_of_frames) {
					if (!is_looping) {
						has_finished = true;
					} else {
						current_frame_number = 0;
					}
				}
			}
		}
	}
	
	public void draw() {
		if (!has_finished)
			graphics.drawq(image, quads[current_frame_number], x, y);
	}
	
	public boolean hasFinished() {
		return has_finished;
	}
}
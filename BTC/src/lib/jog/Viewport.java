package lib.jog;

public class Viewport {
	
	public String name;
	public final int
		x, y,
		width, height;
	

	public Viewport(){
		this.x = 0;
		this.y = 0;
		this.width = 0;
		this.height = 0;
	}
	
	public Viewport(String name, int x, int y, int width, int height) {
		this.name = name;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Viewport(String name, Viewport v){
		this.name = name;
		this.x = v.x;
		this.y = v.y;
		this.width = v.width;
		this.height = v.height;
	}

	@Override
	public String toString() {
		return String.format("<Viewport: %1$s> (%2$dx%3$d) @ (%4$d, %5$d)",
		                                 name,  width,height, x,y);
	}	

}

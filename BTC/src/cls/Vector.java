package cls;

/**
 * Simplified 3D vector class with basic operations
 * @author Huw Taylor
 */
public class Vector {
	private double x, y, z;
	
	/**
	 * Constructor for a vector
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param z Z coordinate
	 */
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	
	public double getZ() {
		return z;
	}
	
	public void setZ(double z) {
		this.z = z;
	}	

	/**
	 * Checks a vector for equality with this vector.
	 * @param Object o the object to be tested for equality
	 * @return a boolean result of the equality test.
	 */
	@Override
	public boolean equals(Object o) {
		if (o.getClass() != Vector.class) { 
			return false;
		} else {
			Vector v = (Vector) o;
			return (x == v.getX()) && (y == v.getY()) && (z == v.getZ());
		}
	}
	
	/**
	 * Calculates the magnitude of the vector
	 * @return the magnitude of the vector
	 */
	public double magnitude() {
		return Math.sqrt(magnitudeSquared());
	}
	
	public double magnitudeSquared() {
		return (x*x) + (y*y) + (z*z);
	}
	
	/**
	 * Normalises the vector
	 * @return a normalised vector
	 */
	public Vector normalise() {
		return this.scaleBy(1/magnitude());
	}
	
	/**
	 * Scales the vector by a given scalar
	 * @param n the scalar to scale by
	 * @return a new scaled vector
	 */
	public Vector scaleBy(double n) {
		return new Vector(x * n, y * n, z * n);
	}
	
	/**
	 * Adds two vectors together
	 * @param v a vector to be added
	 * @return the sum of the vectors
	 */
	public Vector add(Vector v) {
		return new Vector(x + v.getX(), y + v.getY(), z + v.getZ());
	}
	
	/**
	 * Subtracts two vectors
	 * @param v a vector to be subtracted
	 * @return the result of the subtractions
	 */
	public Vector sub(Vector v) {
		return add(v.scaleBy(-1)); // Invert numbers and add
	}
	
	/**
	 * Gets the angle between this vector and a specified vector
	 * @param v the vector to find the angle to
	 * @return the angle between this vector and another
	 */
	public double angleBetween(Vector v) {
		double a = Math.acos( (x*v.x + y*v.y + z*v.z) / (magnitude() * v.magnitude()));
		if (v.y < y)
			a *= -1;
		return a;
	}
}
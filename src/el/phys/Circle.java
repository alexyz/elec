
package el.phys;

public class Circle {
	/**
	 * Current position and radius
	 */
	public float x, y, radius;
	
	public Circle() {
		//
	}

	public Circle(float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	@Override
	public String toString() {
		return String.format("C[%.1f,%.1f,%.1f]", x, y, radius);
	}
}

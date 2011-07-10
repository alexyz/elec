package el.phys;

public class Circle {
	public Circle(float x, float y, float r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}
	/**
	 * Current position and radius
	 */
	public float x, y, r;
	@Override
	public String toString() {
		return String.format("C[%.1f,%.1f:%.1f]", x, y, r);
	}
}

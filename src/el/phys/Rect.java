package el.phys;

/**
 * A simple float rectangle defined by top left corner and bottom right corner.
 * The width and height can be calculated
 */
public class Rect {
	public float x0, y0, x1, y1;

	public Rect(float x0, float y0, float x1, float y1) {
		this.x0 = x0;
		this.y0 = y0;
		this.x1 = x1;
		this.y1 = y1;
		if (x0 > x1 || y0 > y1) {
			throw new RuntimeException();
		}
	}

	public float width() {
		return x1 - x0;
	}

	public float height() {
		return y1 - y0;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("Rect[%.1f,%.1f-%.1f,%.1f]", x0, y0, x1, y1);
	}
}

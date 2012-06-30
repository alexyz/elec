package el.fg;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Exhaust
 */
public class Exhaust extends TransObject {
	
	private static final float lifet = 0.5f;
	private static final float defradius = 5f;
	
	public Exhaust(float x, float y, float dx, float dy, float t) {
		super(t, t + lifet);
		this.x = x;
		this.y = y;
		this.radius = defradius;
		this.vx = dx;
		this.vy = dy;
		this.reflect = false;
		this.collideBackground = false;
	}
	
	@Override
	public void paint(Graphics2D g, float p) {
		Color c = new Color(1f, 1f - p, 0, 1f - p);
		g.setColor(c);
		int r = (int) (2 + (p * 5));
		g.fillOval(-r, -r, r * 2, r * 2);
	}
	
	@Override
	public String toString() {
		return String.format("Exhast[]") + super.toString();
	}
	
}

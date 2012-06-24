package el.fg;
import java.awt.Color;
import java.awt.Graphics2D;

import el.phys.Circle;

/**
 * Exhaust
 */
public class Exhaust extends TransMovingFgObject {
	
	private static final float lifet = 0.5f;
	private static final float radius = 5f;
	private static final Color[] cols = new Color[10];
	
	static {
		cols[0] = new Color(255,192,0);
		for (int n = 1; n < cols.length; n++) {
			cols[n] = cols[n-1].darker();
		}
	}
	
	public Exhaust(float x, float y, float dx, float dy, float t) {
		super(new Circle(x, y, radius), t, t + lifet);
		this.vx = dx;
		this.vy = dy;
		this.reflect = false;
		this.collide = false;
    }
	
	@Override
	public void paint(Graphics2D g, float p) {
		g.setColor(cols[Math.min((int)(p*10), cols.length - 1)]);
		int r = (int) (2 + (p*5));
		g.fillOval(-r, -r, r * 2, r * 2);
	}
	
	@Override
	public String toString() {
		return String.format("Exh[]") + super.toString();
	}
	
	
}
	
package el.fg;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * An explosion
 */
public class ExplodeObject extends TransObject {
	private static final int lifet = 1;
	public ExplodeObject(float x, float y, float radius, float t) {
		super(t, t + lifet);
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.reflect = false;
		this.collideBackground = false;
    }
	@Override
	public void paint(Graphics2D g, float p) {
		g.setColor(new Color(1f, 1f - p, 0f, 1f - p));
		int r = (int) (radius + (radius * 4 * p));
		g.fillOval(-r, -r, r*2, r*2);
	}
}

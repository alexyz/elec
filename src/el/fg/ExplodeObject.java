package el.fg;

import java.awt.Color;
import java.awt.Graphics2D;

import el.phys.Circle;

public class ExplodeObject extends TransObject {
	private static final int lifet = 1;
	public ExplodeObject(float x, float y, float radius, float t) {
		super(new Circle(x, y, radius), t, t + lifet);
		this.reflect = false;
		this.collideBackground = false;
    }
	@Override
	public void paint(Graphics2D g, float p) {
		g.setColor(new Color(1f, 1f - p, 0f, 1f - p));
		int r = (int) (c.r + (c.r * 4 * p));
		g.fillOval(-r, -r, r*2, r*2);
	}
}

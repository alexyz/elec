package el.bg;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import el.phys.Circle;
import el.phys.IntersectionFunction;
import el.phys.Intersection;
import el.phys.Rect;

/**
 * A map tile with useful methods
 */
public class Tile extends Rect {
	public Tile(int x, int y) {
		super(x, y, x+32, y+32);
	}
	public boolean isat(int x, int y) {
		return x >= x0 && x < x1 && y >= y0 && y <= y1;
	}
	public boolean isin(float mx0, float my0, float mx1, float my1) {
		return x1 >= mx0 && x0 <= mx1 && y0 >= my0 && y1 <= my1;
	}

	public void paint(Graphics2D g, int sx, int sy) {
		g.setColor(Color.gray);
		int w = (int) width();
		g.fillRect(sx, sy, w, w);
	}
	public Intersection intersects(IntersectionFunction i, Circle c, float tx, float ty) {
		return i.intersect(this, c, tx, ty, 0.75f);
	}
}

package el.bg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import el.ClientMain;
import el.phys.Circle;
import el.phys.Intersect;
import el.phys.Intersection;
import el.phys.Rect;

/**
 * A map tile with useful methods
 */
public class Tile extends Rect {
	private final Image i;
	
	public Tile(int x, int y) {
		super(x, y, x + 32, y + 32);
		i = ClientMain.getImage(ClientMain.TILE1_IMAGE);
	}
	
	public boolean isat(int x, int y) {
		return x >= x0 && x < x1 && y >= y0 && y <= y1;
	}
	
	public boolean isin(float mx0, float my0, float mx1, float my1) {
		return x1 >= mx0 && x0 <= mx1 && y0 >= my0 && y1 <= my1;
	}
	
	public void paint(Graphics2D g, int sx, int sy) {
		//g.setColor(Color.gray);
		//int w = (int) width();
		//g.fillRect(sx, sy, w, w);
		g.drawImage(i, sx, sy, null);
	}
	
	public Intersection intersects(Intersect i, Circle c, float tx, float ty) {
		return i.intersect(this, c, tx, ty, 0.75f);
	}
}

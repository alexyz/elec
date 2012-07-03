package el.bg;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.StringTokenizer;

import el.ClientFrame;
import el.phys.Circle;
import el.phys.Intersection;
import el.phys.Rect;
import el.phys.cs.CSIntersect;

/**
 * A map tile with useful methods
 */
public class Tile extends Rect {
	
	/**
	 * Create tile from string
	 */
	public static Tile read(StringTokenizer tokens) {
		int x = Integer.parseInt(tokens.nextToken());
		int y = Integer.parseInt(tokens.nextToken());
		return new Tile(x, y);
	}
	
	private final Image image;
	
	public Tile(int x, int y) {
		super(x, y, x + 32, y + 32);
		image = ClientFrame.getImage(ClientFrame.TILE1_IMAGE);
	}
	
	/**
	 * write tile to string
	 */
	public void write(StringBuilder sb) {
		sb.append((int) x0).append(" ");
		sb.append((int) y0).append(" ");
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
		g.drawImage(image, sx, sy, null);
	}
	
	public Intersection intersects(Circle c, float tx, float ty) {
		// XXX bf should be 1 for bombs
		return CSIntersect.intersect(this, c, tx, ty, 0.75f);
	}
}

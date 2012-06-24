package el.bg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.*;

import el.fg.FgObject;
import el.phys.Circle;
import el.phys.Intersect;
import el.phys.Intersection;

/**
 * the map tiles
 */
public class MapBgObject extends BgObject {
	
	// load and save
	// place and delete
	
	// TODO use a map
	ArrayList<Tile> tiles = new ArrayList<Tile>();
	
	public MapBgObject() {
		tiles.add(new Tile(500000, 499920));
		tiles.add(new Tile(500100, 499920));
		Random r = new Random();
		for (int n = 0; n < 10; n++) {
			tiles.add(new Tile(499500+r.nextInt(1000),499500+r.nextInt(1000)));
		}
	}
	
	@Override
	public void paint(Graphics2D g, float mx1, float my1, int w, int h) {
		final float mx2 = mx1 + w, my2 = my1 + h;
		// g.setColor(Color.white);
		// g.drawString(String.format("map x %.1f to %.1f, y %.1f to %.1f", mx1,
		// mx2, my1, my2), 10, 70);
		for (Tile t : tiles) {
			if (t.isin(mx1, my1, mx2, my2)) {
				int sx = (int) (t.x0 - mx1);
				int sy = (int) (t.y0 - my1);
				t.paint(g, sx, sy);
				g.setColor(Color.white);
				g.drawString(".o", sx, sy);
			}
		}
	}
	
	@Override
	public void update(float t, float dt) {
		//
	}
	
	@Override
	public Intersection intersects(Intersect i, Circle c, float tx, float ty) {
		// need to know if line intersects with any tile
		// println("checking map intersect with %f,%f -> %f,%f", lx1, ly1, lx2,
		// ly2);
		Intersection r;
		for (Tile t : tiles)
			if ((r = t.intersects(i, c, tx, ty)) != null)
				return r;
		return null;
	}
	
	public void place(int mx, int my, boolean rem) {
		System.out.println(String.format("place/remove map tile at %d,%d", mx, my));
		Iterator<Tile> i = tiles.iterator();
		while (i.hasNext()) {
			Tile t = i.next();
			if (t.isat(mx, my)) {
				System.out.println("found tile");
				if (rem) {
					System.out.println("removing tile");
					i.remove();
				}
				return;
			}
		}
		System.out.println("placing tile");
		int m = ~0xf;
		tiles.add(new Tile(mx & m, my & m));
	}
	
}
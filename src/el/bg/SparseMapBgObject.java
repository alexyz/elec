package el.bg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.*;

import el.phys.Circle;
import el.phys.Intersection;

/**
 * the map tiles
 */
public class SparseMapBgObject extends MapBgObject {
	
	public static final int ADD_ACTION = 1, REMOVE_ACTION = 3;
	
	// load and save
	// place and delete
	
	// TODO use a map of 10k*10k sectors of 100*100 pixels
	// where key is xq + (yq << 16)
	private final ArrayList<Tile> tiles = new ArrayList<Tile>();
	
	public SparseMapBgObject() {
		//
	}
	
	/**
	 * Create a random map
	 */
	public void init() {
		tiles.add(new Tile(500000 - 64, 500000 + 128));
		tiles.add(new Tile(500000 + 64, 500000 + 128));
	}
	
	/**
	 * Read map from string
	 */
	@Override
	public void read(String data) {
		StringTokenizer tokens = new StringTokenizer(data);
		tiles.clear();
		while (tokens.hasMoreTokens()) {
			tiles.add(Tile.read(tokens));
		}
	}
	
	/**
	 * Write map to string
	 */
	@Override
	public String write() {
		StringBuilder sb = new StringBuilder();
		for (Tile tile : tiles) {
			tile.write(sb);
		}
		return sb.toString();
	}
	
	@Override
	public void paint(Graphics2D g, float mx1, float my1) {
		int w = g.getClipBounds().width, h = g.getClipBounds().height;
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
	public Intersection intersects(Circle c, float tx, float ty) {
		// need to know if line intersects with any tile
		// println("checking map intersect with %f,%f -> %f,%f", lx1, ly1, lx2,
		// ly2);
		
		// FIXME returns first collision and not closest collision
		// could just add all collisions to list and pick closest
		
		Intersection r;
		for (Tile t : tiles)
			if ((r = t.intersects(c, tx, ty)) != null)
				// FIXME need to see if reflection bounces...
				return r;
		return null;
	}
	
	public void setMapTile(int mx, int my, int action) {
		System.out.println(String.format("place/remove map tile at %d,%d", mx, my));
		Iterator<Tile> i = tiles.iterator();
		while (i.hasNext()) {
			Tile t = i.next();
			if (t.isat(mx, my)) {
				System.out.println("found tile");
				if (action == REMOVE_ACTION) {
					System.out.println("removing tile");
					i.remove();
				}
				return;
			}
		}
		System.out.println("placing tile");
		// blank out bottom 16 bits - should depend on tile
		int m = ~0xf;
		tiles.add(new Tile(mx & m, my & m));
	}
	
}
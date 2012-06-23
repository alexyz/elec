package el.bg;
import java.awt.Graphics2D;

import el.phys.Circle;
import el.phys.Intersect;
import el.phys.Intersection;


/**
 * Object without a position (e.g. starfield and map)
 */
public abstract class BgObject {

	/**
	 * Paint object for given top left mx, my (and g.clip.width and height)
	 */
	public void paint(Graphics2D g, float mxo, float myo) {
		int w = g.getClipBounds().width, h = g.getClipBounds().height;
		paint(g, mxo, myo, w, h);
	}
	
	public abstract void paint(Graphics2D g, float mxo, float myo, int w, int h);
	
	public abstract void update (float t, float dt);
	
	public Intersection intersects(Intersect i, Circle c, float tx, float ty) {
		return null;
	}

}


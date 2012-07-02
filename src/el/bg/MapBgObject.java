
package el.bg;

import el.phys.Circle;
import el.phys.Intersection;

public abstract class MapBgObject extends BgObject {
	
	public void setMapTile(int mx, int my, int action) {
		//
	}
	
	/**
	 * Read map from string
	 */
	public void read(String data) {
		//
	}
	
	public String write() {
		return null;
	}
	
	public Intersection intersects(Circle c, float tx, float ty) {
		return null;
	}
	
}

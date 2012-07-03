
package el.bg;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Object without a position (e.g. starfield and map)
 */
public abstract class BgObject {
	
	/**
	 * Paint object for given top left mx, my (and g.clip.width and height)
	 */
	public abstract void paint(Graphics2D g, float mxo, float myo);
	
	public void update(float t, float dt) {
		//
	}
	
	public BufferedImage getRadarImage(float modelx, float modely, float modelw, float modelh) {
		return null;
	}
	
}

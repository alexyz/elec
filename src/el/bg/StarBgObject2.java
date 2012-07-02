
package el.bg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static el.phys.FloatMath.*;

/**
 * draws a nice parallax star field
 */
public class StarBgObject2 extends BgObject {
	
	private static final int stars_per_mp = 200;
	private static final float max_star_depth = 3;
	private static final int max_star_diam = 3;
	
	/** all star data [z, diameter, x] */
	private final float[][] stars;
	/** star colours */
	private final Color[][] colours;
	private final Random rnd = new Random();
	
	/** current stars */
	private float[][] stars2 = new float[0][];
	private float lastUpdate;
	private int paintCount = 0;
	
	public StarBgObject2() {
		
		stars = new float[stars_per_mp][3];
		colours = new Color[stars.length][2];
		
		for (int n = 0; n < stars.length; n++) {
			for (int m = 0; m < stars[n].length; m++) {
				// z (1 to max+1 biased to max)
				stars[n][0] = (1f - sq(rnd.nextFloat())) * max_star_depth + 1;
				// diameter (2 to max+2 biased to 2, as less than 2 doesn't draw)
				stars[n][1] = sq(rnd.nextFloat()) * max_star_diam + 2;
				// x (0-1)
				stars[n][2] = rnd.nextFloat();
			}
			
			int r = rnd.nextInt(64) + 192;
			int g = rnd.nextInt(Math.max(r, 64));
			int b = rnd.nextInt(Math.max(g, 1));
			Color c = new Color(r, g, b);
			colours[n][0] = c;
			colours[n][1] = rnd.nextBoolean() ? c.brighter() : c.darker();
		}
	}
	
	@Override
	public void update(float t, float dt) {
		// TODO make the stars flicker
		if (t - lastUpdate > 0.125) {
			lastUpdate = t;
			//
		}
	}
	
	@Override
	public void paint(Graphics2D g, final float mx, final float my) {
		final int w = g.getClipBounds().width, h = g.getClipBounds().height;
		
		// get screen area in mega pixels (1 max) and adjust number of stars to paint
		float a = Math.min((w * h) / 1000000f, 1f);
		int c = (int) (a * stars.length);
		if (stars2.length != c) {
			stars2 = Arrays.copyOf(stars, c);
			Arrays.sort(stars2, new StarCmp());
		}
		//g.setColor(Color.gray);
		//g.drawString(String.format("st=%d/%d", stars2.length, stars.length), 5, 100);
		
		// paint the stars
		for (int n = 0; n < stars2.length; n++) {
			float[] s = stars2[n];
			// don't actually need x or y, though they tend to clump on diagonals
			// so just add a random x offset
			float x = s[2] * w;
			// divide x,y by z to get perspective
			float z = s[0];
			int mx2 = (int) ((mx + x) / z), my2 = (int) (my / z);
			// mod by screen res to get all stars on screen and wrap at edges
			int sx = mx2 % w, sy = my2 % h;
			
			g.setColor(colours[n][paintCount & 1]);
			int d = (int) s[1];
			g.fillOval(w - sx, h - sy, d, d);
			//g.drawString(" " + n + "," + d, w - sx, h - sy);
		}
		
		paintCount++;
	}
	
}

/** sort stars by z order, deepest first */
class StarCmp implements Comparator<float[]> {
	
	@Override
	public int compare(float[] s1, float[] s2) {
		return (int) Math.signum(s2[0] - s1[0]);
	}
	
}

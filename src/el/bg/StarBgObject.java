
package el.bg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class StarBgObject extends BgObject {
	
	private static final Color gridColour = new Color(96, 0, 0);
	final float[][] stars = new float[50][3];
	final Color[] colours = new Color[stars.length];
	
	public StarBgObject() {
		Random rnd = new Random();
		for (int n = 0; n < stars.length; n++) {
			for (int m = 0; m < stars[n].length; m++)
				stars[n][m] = rnd.nextFloat();
			int r = rnd.nextInt(256);
			int g = rnd.nextInt(Math.max(r, 1));
			colours[n] = new Color(r, g, 0);
		}
	}
	
	@Override
	public void update(float t, float dt) {
		// TODO make the stars flicker
		// TODO make the stars drift a bit as well
	}
	
	@Override
	public void paint(Graphics2D g, float mx, float my) {
		int w = g.getClipBounds().width, h = g.getClipBounds().height;
		g.setBackground(Color.black);
		g.clearRect(0, 0, w, h);
		
		/*
		g.setColor(gridColour);
		int size = 200;
		for (float x = mx - (mx % size); x < mx + w; x+=size) {
			for (float y = my - (my % size); y < my + h + 20; y+=size) {
				int vx = (int) (x - mx), vy = (int) (y - my);
				g.drawLine(vx - 5, vy, vx + 5, vy);
				g.drawLine(vx, vy - 5, vx, vy + 5);
				g.drawString(x + "," + y, vx + 5, vy - 5);
			}
		}
		*/
		
		// TODO need indefinite layers, not just two
		paint1(g, (int) mx, (int) my, 512);
		paint1(g, (int) (mx / 2), (int) (my / 2), 256);
	}
	
	private void paint1(Graphics2D g, int mx, int my, int quadsize) {
		int w = g.getClipBounds().width, h = g.getClipBounds().height;
		// get the quadrants
		int qx1 = mx / quadsize, qy1 = my / quadsize;
		int qx2 = (mx + w) / quadsize, qy2 = (my + h) / quadsize;
		for (int qx = qx1; qx <= qx2; qx++) {
			// convert quadrant back to screen
			int sx = qx * quadsize - mx;
			for (int qy = qy1; qy <= qy2; qy++) {
				int sy = qy * quadsize - my;
				paint2(g, sx, sy, quadsize);
			}
		}
	}
	
	private void paint2(Graphics2D g, int sx, int sy, int qs) {
		int w = g.getClipBounds().width, h = g.getClipBounds().height;
		for (int n = 0; n < stars.length; n++) {
			float[] s = stars[n];
			int x = (int) (s[0] * qs) + sx;
			int y = (int) (s[1] * qs) + sy;
			int d = (int) (s[2] * 3);
			if (x > -d && y > -d && x < w + d && y < h + d) {
				g.setColor(colours[n]);
				g.drawOval(x, y, d, d);
			}
		}
	}
}

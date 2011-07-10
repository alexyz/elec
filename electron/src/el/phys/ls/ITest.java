package el.phys.ls;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JFrame;

class ITest {
	
	public static void main(String[] args) {
		
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new XJComponent());
		f.pack();
		f.setVisible(true);
		
	}
	

}

class XJComponent extends JComponent {
	int sx1 = 150, sy1 = 150, sx2 = 350, sy2 = 350;
	int lx1 = 50, ly1 = 50, lx2 = 100, ly2 = 100;
	float[] p = new float[12];
	
	public XJComponent() {
		setPreferredSize(new Dimension(640,480));
		MouseAdapter m = new MouseAdapter() {
			@Override
			public void mousePressed(java.awt.event.MouseEvent e) {
				lx1 = e.getPoint().x;
				ly1 = e.getPoint().y;
				repaint();
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				lx2 = e.getPoint().x;
				ly2 = e.getPoint().y;
				repaint();
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				lx2 = e.getPoint().x;
				ly2 = e.getPoint().y;
				Arrays.fill(p, 0);
				intersectsdebug(p, sx1, sy1, sx2, sy2, lx1, ly1, lx2, ly2);
				repaint();
			}
		};
		addMouseListener(m);
		addMouseMotionListener(m);
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(Color.black);
		g.drawRect(sx1, sy1, sx2 - sx1, sy2 - sy1);
		g.drawLine(lx1, ly1, lx2, ly2);
		g.drawString("s1", sx1, sy1);
		g.drawString("s2", sx2, sy2);
		g.drawString("l1", lx1, ly1);
		g.drawString("l2", lx2, ly2);
		
		for (int i = 0; i < 12; i+=2) {
			int x = (int) p[i], y = (int) p[i+1];
			if (i == 8) {
				g.drawString("   <-", x, y);
			} else {
				g.fillOval(x - 2, y - 2, 4, 4);
				g.drawString("i" + i, x, y);
			}
		}
		// reflection
		g.drawLine((int) p[8], (int) p[9], (int) p[10], (int) p[11]);
	}
	

	/**
	 * Return true if line intersects with square.
	 * Optionally returns first intersection point (from l1 to l2).
	 */
	public static boolean intersectsdebug(float[] ret, float sx1, float sy1, float sx2, float sy2, 
			float lx1, float ly1, float lx2, float ly2) {
		
		// consider line l1->l2 to be parameterised, e.g.
		// any point on line = l1 + p(l2-l1) where p is 0 to 1
		// now find values of p for the top, bottom, left and right of square (might not be 0-1)

		// FIXME may div by 0...

		// x interception
		final float lx = lx2-lx1;
		final float ly = ly2-ly1;
		
		float xi0 = (sx1 - lx1) / lx;
		ret[0] = lx1 + (lx * xi0);
		ret[1] = ly1 + (ly * xi0);

		float xi1 = (sx2 - lx1) / lx;
		ret[2] = lx1 + (lx * xi1);
		ret[3] = ly1 + (ly * xi1);

		// y interception
		float yi0 = (sy1 - ly1) / ly;
		ret[4] = lx1 + (lx * yi0);
		ret[5] = ly1 + (ly * yi0);

		float yi1 = (sy2 - ly1) / ly;
		ret[6] = lx1 + (lx * yi1);
		ret[7] = ly1 + (ly * yi1);

		// if parameters for both x and y overlap, it intersects
		// there are usually two points of intersection, return the one closest to l1 only

		float i, r;
		if (xi0 > xi1) {
			// make sure xi1 is greater than xi0
			i = xi0;
			xi0 = xi1;
			xi1 = i;
		}
		if (yi0 > yi1) {
			// make sure yi1 is greater than yi0
			i = yi0;
			yi0 = yi1;
			yi1 = i;
		}
		if (xi1 >= yi0 && xi0 <= yi1) {
			// if i0..i2 and i4..i6 overlap
			// pick the second lowest of all four (either i0 or i4)
			if (xi0 > yi0) {
				i = xi0;
				r = -1f;
			} else {
				i = yi0;
				r = 1f;
			}
			
			if (i < 0f || i > 1f) {
				// line would intersect but it is not long enough
				System.out.println("does not reach");
				return false;
			}
			System.out.println("intersects at " + i);

		} else {
			System.out.println("does not overlap");
			return false;
		}

		// turn parameter back into x,y position
		ret[8] = lx1 + (lx * i);
		ret[9] = ly1 + (ly * i);
		// reflected point
		ret[10] = ret[8] + (r * lx * (1f - i));
		ret[11] = ret[9] + (r * -1f * ly * (1f - i));
		return true;

	}
	

}
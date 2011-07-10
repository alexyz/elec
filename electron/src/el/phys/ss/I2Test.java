package el.phys.ss;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;

import el.phys.Intersection;

class I2Test {
	
	public static void main(String[] args) {
		
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new C());
		f.pack();
		f.setVisible(true);
		
	}

	public static boolean squareIntersect_(final float[] r, final Rect s, final Rect t, final float tdx, final float tdy) {
		// find closest x/y edge
		final float tx, sx;
		if (tdx <= 0) {
			tx = t.x;
			sx = s.x + s.w;
		} else {
			tx = t.x + t.w;
			sx = s.x;
		}
		
		final float ty, sy;
		if (tdy <= 0) {
			ty = t.y;
			sy = s.y + s.h;
		} else {
			ty = t.y + t.h;
			sy = s.y;
		}
		
		//System.out.printf("x1,y1=%f,%f x2,y2=%f,%f\n", tx, ty, sx, sy);
		
		r[0] = tx;
		r[1] = ty;
		r[2] = sx;
		r[3] = sy;
		
		// find param of x/y collision
		// avoid div by 0 (not a runtime exception with floats)
		final float p1 = tdx != 0f ? (sx - tx) / tdx : 0f;
		final float p1x = tx + (tdx * p1);
		final float p1y = ty + (tdy * p1);
		r[4] = p1x;
		r[5] = p1y;
		final float p2 = tdy != 0f ? (sy - ty) / tdy : 0f;
		final float p2x = tx + (tdx * p2);
		final float p2y = ty + (tdy * p2);
		r[6] = p2x;
		r[7] = p2y;
		System.out.printf("p1=%f p2=%f\n", p1, p2);
		System.out.printf("p1x,p1y=%f,%f p2x,p2y=%f,%f\n", p1x, p1y, p2x, p2y);
		
		final float i, xf, yf;
		if (p1 == p2 && p1 >= 0f && p1 <= 1f) {
			System.out.println("hitdiag");
			i = p1;
			xf = -1f;
			yf = -1f;
			
		} else if (p1 > p2 && p1 >= 0f && p1 <= 1f && p1y >= s.y - t.h && p1y <= s.y + s.h + t.h) {
			System.out.println("hitx");
			i = p1;
			xf = -1f;
			yf = 1f;
			
		} else if (p2 > p1 && p2 >= 0f && p2 <= 1f && p2x >= s.x - t.w && p2x <= s.x + s.w + t.w) {
			System.out.println("hity");
			i = p2;
			xf = 1f;
			yf = -1f;
			
		} else {
			return false;
		}
		
		// translate t to the intersection point, then reflect
		final float rx = t.x + (tdx * i) + xf * tdx * (1f - i);
		final float ry = t.y + (tdy * i) + yf * tdy * (1f - i);
		
		r[8] = rx;
		r[9] = ry;
		
		return true;
	}


}



class C extends JComponent {
	//float[] r = new float[10];
	Intersection r;
	Rect s = new Rect(150, 150, 150, 150);
	Rect t = new Rect(50, 50, 50, 50);
	int dx = 50, dy = 50;
	
	public C() {
		setPreferredSize(new Dimension(640,480));
		MouseAdapter m = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int x = e.getPoint().x, y = e.getPoint().y;
				if (e.getButton() == MouseEvent.BUTTON1) {
					t.x = x;
					t.y = y;
				}
				repaint();
			}
			public void mouseDragged(MouseEvent e) {
				int x = e.getPoint().x, y = e.getPoint().y;
				if (e.getButton() == MouseEvent.BUTTON1) {
					t.w = x - t.x;
					t.h = y - t.y;
				} else {
					dx = x - (int) (t.x + t.w / 2);
					dy = y - (int) (t.y + t.h / 2);
				}
				repaint();
			}
			public void mouseReleased(MouseEvent e) {
				r = Impl.squareIntersect(s.x, s.y, s.x + s.w, s.y + s.h, t.x, t.y, t.x + t.w, t.y + t.h, dx, dy);
				repaint();
			}
		};
		addMouseListener(m);
		addMouseMotionListener(m);
	}

	public void paint(Graphics g) {
		g.setColor(Color.black);
		int sx = (int) s.x, sy = (int) s.y, sw = (int) s.w, sh = (int) s.h;
		int tx = (int) t.x, ty = (int) t.y, tw = (int) t.w, th = (int) t.h;
		
		g.drawRect(sx, sy, sw, sh);
		g.drawString("s", sx, sy);
		g.drawRect(tx, ty, tw, th);
		g.drawString("t", tx, ty);
		g.drawRect(tx + dx, ty + dy, tw, th);
		g.drawString("d", tx + dx, ty + dy);
		
		g.setColor(Color.green);
		g.drawLine(tx, ty, tx + dx, ty + dy);
		g.drawLine(tx + tw, ty, tx + tw + dx, ty + dy);
		g.drawLine(tx + tw, ty + th, tx + tw + dx, ty + th + dy);
		g.drawLine(tx, ty + th, tx + dx, ty + th + dy);
		
		if (r != null) {
			g.setColor(Color.red);
			
			int ivx = (int) r.itx, ivy = (int) r.ity;
			g.drawRect(tx + ivx, ty + ivy, tw, th);
			g.drawString("i", tx + ivx, ty + ivy);
			
			int rvx = (int) r.rtx, rvy = (int) r.rty;
			g.drawRect(tx + rvx, ty + rvy, tw, th);
			g.drawString("r", tx + rvx, ty + rvy);
		}
	}
	
	

}
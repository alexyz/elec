package el.phys.cs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JFrame;

import el.phys.Circle;
import el.phys.Intersection;
import el.phys.Rect;

class I3Test extends JComponent implements MouseWheelListener, MouseListener, MouseMotionListener {
	
	static float _x0, _y0, _x1, _y1, _x2, _y2, _x3, _y3;
	
	public static void main(String[] args) {
		
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new I3Test());
		f.pack();
		f.setVisible(true);
		
	}
	
	int sx1 = 150, sy1 = 150, sx2 = 350, sy2 = 350;
	int lx1 = 50, ly1 = 50, lx2 = 100, ly2 = 100;
	int mx = 0, my = 0;
	int rad = 50;
	float bounce = 1f;
	Intersection i;
	
	public I3Test() {
		setPreferredSize(new Dimension(640,480));
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	
	public void mousePressed(java.awt.event.MouseEvent e) {
		int x = e.getPoint().x;
		int y = e.getPoint().y;
		if (e.getButton() == MouseEvent.BUTTON1) {
			lx1 = x;
			ly1 = y;
		} else {
			sx1 = x;
			sy1 = y;
		}
		repaint();
	}

	public void mouseDragged(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		
		if (e.getButton() == MouseEvent.BUTTON1) {
			lx2 = mx;
			ly2 = my;
		} else {
			sx2 = mx;
			sy2 = my;
		}
		repaint();
	}
	
	public void mouseMoved(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		repaint();
	}

	public void mouseReleased(MouseEvent e) {
		redo();
		repaint();
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isShiftDown()) {
			bounce += (e.getWheelRotation() / 128f);
		} else {
			rad += e.getWheelRotation();
		}
		redo();
		repaint();
	}
	
	public void mouseClicked(MouseEvent e) {
		//
	}

	public void mouseEntered(MouseEvent e) {
		//
	}

	public void mouseExited(MouseEvent e) {
		//
	}

	void redo() {
		float tx = lx2 - lx1;
		float ty = ly2 - ly1;
		Rect r = new Rect(sx1, sy1, sx2, sy2);
		Circle c = new Circle(lx1, ly1, rad);
		i = intersectsdebug(r, c, tx, ty, bounce); 
		
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(Color.yellow);
		g.drawRect(sx1 - rad, sy1 - rad, sx2 - sx1 + rad * 2, sy2 - sy1 + rad * 2);
		g.drawOval(sx1 - rad, sy1 - rad, rad * 2, rad * 2);
		g.drawOval(sx2 - rad, sy1 - rad, rad * 2, rad * 2);
		g.drawOval(sx1 - rad, sy2 - rad, rad * 2, rad * 2);
		g.drawOval(sx2 - rad, sy2 - rad, rad * 2, rad * 2);
		
		g.setColor(Color.black);
		g.drawString(mx + ", " + my, 20, 20);
		g.drawString("bounce " + bounce, 20, 40);
		g.drawRect(sx1, sy1, sx2 - sx1, sy2 - sy1);
		g.drawLine(lx1, ly1, lx2, ly2);
		g2.drawOval(lx1 - rad, ly1 - rad, rad * 2, rad * 2);
		g.drawString(" r", sx1, sy1);
		g.drawString(" c", lx1, ly1);
		
		g2.drawString(".l", _x0, _y0);
		g2.drawString(".r", _x1, _y1);
		g2.drawString(".t", _x2, _y2);
		g2.drawString(".b", _x3, _y3);
		
		if (i != null) {
			g2.setColor(Color.red);
			int px = (int) (lx1 + i.itx);
			int py = (int) (ly1 + i.ity);
			g2.drawOval(px - rad, py - rad, rad * 2, rad * 2);
			
			g2.setColor(Color.blue);
			int rx = (int) (lx1 + i.rtx);
			int ry = (int) (ly1 + i.rty);
			g2.drawString(".p", rx, ry);
			g2.drawOval(rx - rad, ry - rad, rad * 2, rad * 2);
		}
	}
	
	/**
	 * Return true if line intersects with square.
	 * Optionally returns first intersection point (from l1 to l2).
	 */
	public static Intersection intersectsdebug(Rect r, Circle c, float tx, float ty, float bf) {
		
		float rx0 = r.x0 - c.r;
		float rx1 = r.x1 + c.r;
		float ry0 = r.y0 - c.r;
		float ry1 = r.y1 + c.r;

		float px0 = (rx0 - c.x) / tx;
		//_x0 = c.x + tx * px0;
		//_y0 = c.y + ty * px0;

		float px1 = (rx1 - c.x) / tx;
		//_x1 = c.x + tx * px1;
		//_y1 = c.y + ty * px1;
		
		float py0 = (ry0 - c.y) / ty;
		//_x2 = c.x + tx * py0;
		//_y2 = c.y + ty * py0;
		
		float py1 = (ry1 - c.y) / ty;
		//_x3 = c.x + tx * py1;
		//_y3 = c.y + ty * py1;


		// if parameters for both x and y overlap, it intersects
		// there are usually two points of intersection, return the one closest to l1 only

		float pxh, pxl;
		if (px0 > px1) {
			pxh = px0;
			pxl = px1;
		} else {
			pxh = px1;
			pxl = px0;
		}
		
		float pyh, pyl;
		if (py0 > py1) {
			pyh = py0;
			pyl = py1;
		} else {
			pyh = py1;
			pyl = py0;
		}

		if (!(pxh >= pyl && pxl <= pyh)) {
			//System.out.println("does not overlap");
			return null;
		}

		// if i0..i2 and i4..i6 overlap
		// pick the second lowest of all four (either i0 or i4)
		float p, vx;
		if (pxl > pyl) {
			p = pxl;
			vx = -bf;
		} else {
			p = pyl;
			vx = bf;
		}

		if (p < 0f || p > 1f) {
			// line would intersect but it is not long enough
			//System.out.println("does not reach");
			return null;
		}

		float idx = tx * p;
		float idy = ty * p;
		// TODO some way to detect corner miss
		// but it requires the entire path falls outside the corner radius
		// not just the intersection point
		
		//System.out.println("intersects at " + p);
		Intersection in = new Intersection();
		in.itx = idx;
		in.ity = idy;
		// can only bounce in one axis...
		in.vx = vx;
		in.vy = -vx;
		
		// p after reflection, if required
		if (bf > 0) {
			float pp = (1f - p);
			in.rtx = in.itx + (in.vx * tx * pp);
			in.rty = in.ity + (in.vy * ty * pp);
		}

		return in;
	}

	

}
package el.phys.cs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.JComponent;
import javax.swing.JFrame;

import el.phys.Circle;
import el.phys.Intersection;
import el.phys.Point;
import el.phys.Rect;

/**
 * Separate program to test the intersection function
 */
class I3Test extends JComponent implements MouseWheelListener, MouseListener, MouseMotionListener {
	
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
	int rad = 25;
	float bounce = 1f;
	Intersection i;
	int b;
	
	public I3Test() {
		setPreferredSize(new Dimension(640,480));
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}
	
	@Override
	public void mousePressed(java.awt.event.MouseEvent e) {
		System.out.println("pressed " + e.getButton());
		b = e.getButton();
		int x = e.getPoint().x;
		int y = e.getPoint().y;
		if (b == MouseEvent.BUTTON1) {
			lx1 = x;
			ly1 = y;
		} else if (b == MouseEvent.BUTTON3) {
			sx1 = x;
			sy1 = y;
		}
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		
		if (b == MouseEvent.BUTTON1) {
			lx2 = mx;
			ly2 = my;
		} else if (b == MouseEvent.BUTTON3) {
			sx2 = mx;
			sy2 = my;
		}
		repaint();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		mx = e.getX();
		my = e.getY();
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		redo();
		repaint();
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isShiftDown()) {
			bounce += (e.getWheelRotation() / 128f);
		} else if (e.isControlDown()) {
			//theta += (e.getWheelRotation() / 10f);
		} else {
			rad += e.getWheelRotation();
		}
		redo();
		repaint();
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		//
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//
	}

	@Override
	public void mouseExited(MouseEvent e) {
		//
	}

	void redo() {
		float tx = lx2 - lx1;
		float ty = ly2 - ly1;
		Rect r = new Rect(sx1, sy1, sx2, sy2);
		Circle c = new Circle(lx1, ly1, rad);
		i = CSIntersect.intersect(r, c, tx, ty, bounce);
	}
	
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(Color.magenta);
		g.drawRect(sx1 - rad, sy1 - rad, sx2 - sx1 + rad * 2, sy2 - sy1 + rad * 2);
		g.drawOval(sx1 - rad, sy1 - rad, rad * 2, rad * 2);
		g.drawOval(sx2 - rad, sy1 - rad, rad * 2, rad * 2);
		g.drawOval(sx1 - rad, sy2 - rad, rad * 2, rad * 2);
		g.drawOval(sx2 - rad, sy2 - rad, rad * 2, rad * 2);
		
		g.setColor(Color.black);
		g.drawString(mx + ", " + my, 20, 20);
		g.drawString("bounce " + bounce, 20, 40);
		//g.drawString("theta " + theta, 20, 60);
		g.drawString("radius " + rad, 20, 80);
		g.drawRect(sx1, sy1, sx2 - sx1, sy2 - sy1);
		g.drawLine(lx1, ly1, lx2, ly2);
		g2.drawOval(lx1 - rad, ly1 - rad, rad * 2, rad * 2);
		g.drawString(" r", sx1, sy1);
		g.drawString(" c", lx1, ly1);
		
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
	
}

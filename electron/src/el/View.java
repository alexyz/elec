package el;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;

import el.bg.BgObject;
import el.fg.FgObject;
import el.fg.Ship;

import java.util.List;

class View extends JComponent {
	private final Model model;
	
	public View(final Model m) {
		this.model = m;
		// enable tab
		setFocusTraversalKeysEnabled(false);
		setMinimumSize(new Dimension(640, 480));
		setPreferredSize(getMinimumSize());
		addKeyListener(new ViewKeyListener(m));
		//addMouseListener(ml);
		//addMouseMotionListener(ml);
		//addMouseWheelListener(ml);
		setFocusable(true);
		setFont(new Font(Font.DIALOG, Font.BOLD, 12));
		setDoubleBuffered(true);
		setOpaque(true);
		//System.out.println("view double buffered: " + isDoubleBuffered());
		//System.out.println("view opaque: " + isOpaque());
		//System.out.println("view optimised: " + isOptimizedDrawingEnabled());
		//System.out.println("view lightweight: " + isLightweight());
	}
	
	/**
	 * Get the model x origin, i.e. the leftmost visible model x co-ordinate.
	 */
	private int getMxo() {
		return model.getX() - getWidth() / 2;
	}
	
	private int getMyo() {
		return model.getY() - getHeight() / 2;
	}
	
	public void paint(Graphics g_) {
		Graphics2D g = (Graphics2D) g_;
		int mxo = getMxo(), myo = getMyo();
		
		// draw bg
		for (BgObject bg : model.getBgObjects())
			bg.paint(g, mxo, myo);
		
		// draw ships
		paintfg(g, model.getFgObjects());
		
		// draw chaff
		paintfg(g, model.getTransFgObjects());
		
		paintStatus(g);
	}
	
	/**
	 * Paint given objects
	 */
	private void paintfg(Graphics2D g, List<FgObject> objects) {
		int mxo = getMxo(), myo = getMyo();
		// view width, height
		int w = getWidth(), h = getHeight();
		
		for (FgObject o : objects) {
			// object width, height
			int r = o.getRadius();
			int d = r * 2;
			// view x, y
			int tx = o.getX() - mxo;
			int ty = o.getY() - myo;
			
			if (tx >= 0 && tx < w && ty >= 0 && ty < h) {
				
				g.translate(tx, ty);
				o.paint(g);
				
				if (o instanceof Ship) {
					g.setColor(Color.gray);
					g.drawOval(-r, -r, d, d);
					g.drawString(".o", 0, 0);
				}
				
				g.translate(-tx, -ty);
				
			}
		}
	}
	
	private void paintStatus(Graphics2D g) {
		//long f = Runtime.getRuntime().freeMemory() / 1000;
		//long tl = Runtime.getRuntime().totalMemory() / 1000;
		int lh = g.getFontMetrics().getHeight();
		g.setColor(Color.white);
		g.drawString(toString(), 5, lh + 5);
		g.drawString(model.toString(), 5, lh * 2 + 5);
		g.drawString(String.valueOf(model.getFocus()), 5, lh * 3 + 5);
	}

	long oldf, oldt;
	float td;
	public String toString() {
		long f = Runtime.getRuntime().freeMemory();
		if (f > oldf) {
			long t = System.nanoTime();
			td = (t - oldt) / 1000000f;
			oldt = t;
		}
		oldf = f;
		return String.format("View[%d,%d] [d=%d gc=%.2f]", 
				getWidth(), getHeight(), Main.delay(), td);
	}
}

class ViewKeyListener implements KeyListener {
	private final Model m;
	public ViewKeyListener(Model m) {
		this.m = m;
	}
	public void keyTyped(java.awt.event.KeyEvent e) {
		switch (e.getKeyChar()) {
			case '¤':
			case '`':
				m.focusCycle();
				break;
			case '[':
				Main.slower();
				break;
			case ']':
				Main.faster();
				break;
			default:
				//System.out.printf("typed '%c' -> %d\n", e.getKeyChar(), e.getKeyCode());
		}
	}
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		switch (c) {
			case KeyEvent.VK_UP:
				// needs to set isUp
				// or have some queue of actions in the model
				m.action("up");
				break;
			case KeyEvent.VK_DOWN:
				m.action("down");
				break;
			case KeyEvent.VK_LEFT:
				m.action("left");
				break;
			case KeyEvent.VK_RIGHT:
				m.action("right");
				break;
			case KeyEvent.VK_CONTROL:
				m.action("fire1");
				break;
			case KeyEvent.VK_TAB:
				m.action("fire2");
				break;
			case KeyEvent.VK_6:
				m.action("fire3");
				break;
			default:
				//System.out.printf("pressed '%c' -> %d\n", e.getKeyChar(), e.getKeyCode());
		}
	}
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				// needs to set isUp
				// or have some queue of actions in the model
				m.unaction("up");
				break;
			case KeyEvent.VK_DOWN:
				m.unaction("down");
				break;
			case KeyEvent.VK_LEFT:
				m.unaction("left");
				break;
			case KeyEvent.VK_RIGHT:
				m.unaction("right");
				break;
			case KeyEvent.VK_CONTROL:
				m.unaction("fire1");
				break;
			case KeyEvent.VK_TAB:
				m.unaction("fire2");
				break;
			case KeyEvent.VK_ESCAPE:
				System.exit(0);
				break;
			case KeyEvent.VK_6:
				m.unaction("fire3");
				break;
			default:
				//System.out.printf("released '%c' -> %d\n", e.getKeyChar(), e.getKeyCode());
		}
	}
}

class ViewMouseListener extends MouseInputAdapter {
	public void mouseClicked(MouseEvent e) {
		//
	}
	public void mouseDragged(MouseEvent e) {
		//
	}
}

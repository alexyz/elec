package el;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;

import el.bg.BgObject;
import el.fg.FgObject;
import el.fg.Ship;

import java.util.List;

/**
 * Exactly the same as JCView except uses canvas instead of JComponent
 */
class CanvasView extends Canvas {
	private final Model model;
	
	private long freeMem, gcTime;
	private float gcTimeDelta;
	private Point mousePoint;
	
	public CanvasView(final Model model) {
		this.model = model;
		// enable capture of tab key
		setFocusTraversalKeysEnabled(false);
		setMinimumSize(new Dimension(640, 480));
		setPreferredSize(getMinimumSize());
		addKeyListener(new ViewKeyListener(model));
		MouseAdapter l = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Point p = e.getPoint();
				int x = getModelX() + p.x;
				int y = getModelY() + p.y;
				System.out.println("view mouse clicked: " + x + ", " + y);
				model.getMap().place(x, y, e.getButton() == MouseEvent.BUTTON3);
			}
			@Override
			public void mouseMoved(MouseEvent e) {
				mousePoint = e.getPoint();
			}
		};
		addMouseListener(l);
		addMouseMotionListener(l);
		setFocusable(true);
		setFont(new Font(Font.DIALOG, Font.BOLD, 12));
		setIgnoreRepaint(true);
	}
	
	@Override
	public boolean isDoubleBuffered() {
		return true;
	}
	
	@Override
	public boolean isOpaque() {
		// return true as this component entirely paints itself
		return true;
	}
	
	public void paintImmediately(int x, int y, int w, int h) {
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			// creates a blit strategy on os x, should be flip...
			createBufferStrategy(2);
			bs = getBufferStrategy();
		}
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		g.setClip(x, y, w, h);
		paint(g);
		g.dispose();
		bs.show();
	}
	
	/**
	 * Get the model x co-ordinate of 0,0 in the view
	 */
	private int getModelX() {
		return model.getX() - getWidth() / 2;
	}
	
	/**
	 * Get the model y co-ordinate of 0,0 in the view
	 */
	private int getModelY() {
		return model.getY() - getHeight() / 2;
	}
	
	@Override
	public void paint(Graphics g_) {
		Graphics2D g = (Graphics2D) g_;
		int mxo = getModelX(), myo = getModelY();
		
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
	private void paintfg(Graphics2D g, List<? extends FgObject> objects) {
		int mxo = getModelX(), myo = getModelY();
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
		// long f = Runtime.getRuntime().freeMemory() / 1000;
		// long tl = Runtime.getRuntime().totalMemory() / 1000;
		int lh = g.getFontMetrics().getHeight();
		g.setColor(Color.white);
		g.drawString(toString(), 5, lh + 5);
		g.drawString(model.toString(), 5, lh * 2 + 5);
		g.drawString(String.valueOf(model.getFocus()), 5, lh * 3 + 5);
		if (mousePoint != null) {
			int mx = getModelX() + mousePoint.x;
			int my = getModelY() + mousePoint.y;
			g.drawString("Mouse " + mx + ", " + my, 5, lh * 4 + 5);
		}
	}
	
	@Override
	public String toString() {
		long f = Runtime.getRuntime().freeMemory();
		if (f > freeMem) {
			long t = System.nanoTime();
			gcTimeDelta = (t - gcTime) / 1000000f;
			gcTime = t;
		}
		freeMem = f;
		return String.format("View[%d,%d] [d=%d gc=%.2f]", getWidth(), getHeight(), ClientMain.delay(), gcTimeDelta);
	}
}

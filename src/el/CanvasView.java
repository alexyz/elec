package el;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import el.bg.BgObject;
import el.fg.FgObject;
import el.fg.ShipObject;

import java.util.List;

/**
 * Renders the model.
 * Has virtually no state of its own
 */
class CanvasView extends Canvas {
	
	//
	// final fields
	//
	
	private final Model model;
	private final ViewKeyListener keyl;
	
	//
	// mutable fields
	//
	
	private Point mousePoint;
	
	public CanvasView(final Model model) {
		this.model = model;
		// enable capture of tab key
		setFocusTraversalKeysEnabled(false);
		setMinimumSize(new Dimension(640, 480));
		setPreferredSize(getMinimumSize());
		this.keyl = new ViewKeyListener(model);
		addKeyListener(keyl);
		MouseAdapter l = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Point p = e.getPoint();
				int x = getModelX() + p.x;
				int y = getModelY() + p.y;
				System.out.println("view mouse clicked: " + x + ", " + y);
				model.setMapTileReq(x, y, e.getButton());
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
		return model.getFocus().getX() - getWidth() / 2;
	}
	
	/**
	 * Get the model y co-ordinate of 0,0 in the view
	 */
	private int getModelY() {
		return model.getFocus().getY() - getHeight() / 2;
	}
	
	@Override
	public void paint(Graphics g_) {
		Graphics2D g = (Graphics2D) g_;
		Object aa = ClientFrame.getInstance().aa ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, aa);
		
		// clear
		clear(g);
		
		// draw bg
		int modelxo = getModelX(), modelyo = getModelY();
		for (BgObject bg : model.getBgObjects())
			bg.paint(g, modelxo, modelyo);
		
		// draw ships
		paintfg(g, model.getFgObjects());
		
		// draw chaff
		paintfg(g, model.getTransFgObjects());
		
		paintStatus(g);
		
		paintBuffer(g);
		
		paintRadar(g, modelxo, modelyo);
	}
	
	private void clear(Graphics2D g) {
		final int w = g.getClipBounds().width, h = g.getClipBounds().height;
		g.setBackground(Color.black);
		// XXX this is REALLY slow on os x
		// takes about 40% of total render time
		g.clearRect(0, 0, w, h);
	}
	
	/**
	 * paint the keyboard buffer and the messages to the screen
	 */
	private void paintBuffer(Graphics2D g) {
		int h = g.getFontMetrics().getHeight();
		int y = getHeight() - h;
		if (keyl.buf.length() > 0) {
			// totally arbitrary guess to get box to go around string
			g.drawRect(5, y - h + 2, getWidth() - 10, h);
			g.drawString(keyl.buf.toString(), 10, y);
			y -= h;
		}
		List<Msg> msgs = model.getMsgs();
		for (int n = 0, m = msgs.size() - 1; n < 5 && m >= 0; n++, m--) {
			Msg msg = msgs.get(m);
			if (msg.sender != null) {
				g.setColor(Color.yellow);
				g.drawString(msg.sender + " > " + msg.msg, 10, y);
			} else {
				g.setColor(Color.white);
				g.drawString(msg.msg, 10, y);
			}
			y -= h;
		}
	}
	
	/**
	 * Paint given objects
	 */
	private void paintfg(Graphics2D g, List<? extends FgObject> objects) {
		int mxo = getModelX(), myo = getModelY();
		// view width, height
		int w = getWidth(), h = getHeight();
		int selfid = model.getId();
		
		for (FgObject o : objects) {
			// object width, height
			int r = o.getRadius();
			// view x, y
			int tx = o.getX() - mxo;
			int ty = o.getY() - myo;
			
			if (tx >= 0 && tx < w && ty >= 0 && ty < h) {
				
				g.translate(tx, ty);
				o.paint(g);
				
				if (o instanceof ShipObject) {
					// paint player name if not self
					if (o.getId() != selfid) {
						String name = model.getPlayers().get(o.getId());
						if (name != null) {
							g.setColor(Color.yellow);
							g.drawString(name, r, -r);
						}
					}
				}
				
				g.translate(-tx, -ty);
				
			}
		}
	}
	
	private void paintStatus(Graphics2D g) {
		// long f = Runtime.getRuntime().freeMemory() / 1000;
		// long tl = Runtime.getRuntime().totalMemory() / 1000;
		int lh = g.getFontMetrics().getHeight();
		g.setColor(Color.lightGray);
		
		g.drawLine(0,0,(int)(getWidth() * ClientFrame.getInstance().busy),0);
		
		long fm = Runtime.getRuntime().freeMemory();
		int delay = ClientFrame.getInstance().getDelay();
		float fps = 1000f / delay;
		
		g.drawString(String.format("Main[delay=%dms fps=%.1f freemem=%.0fMB]",
				delay, fps, 
				fm / 1000000.0), 5, lh + 5);
		
		g.drawString(toString(), 5, lh * 2 + 5);
		g.drawString(model.toString(), 5, lh * 3 + 5);
		g.drawString(String.valueOf(model.getFocus()), 5, lh * 4 + 5);
		if (mousePoint != null) {
			int mx = getModelX() + mousePoint.x;
			int my = getModelY() + mousePoint.y;
			g.drawString("Mouse " + mx + ", " + my, 5, lh * 5 + 5);
		}
	}
	
	private void paintRadar(Graphics2D g, float modelx, float modely) {
		int w = getWidth();
		int h = getHeight();
		int rw = (int) (w * 0.20f), rh = (int) (h * 0.20f);
		int x = w - rw - 5, y = h - rh - 5; 
		g.setColor(Color.gray);
		BufferedImage im = model.getMap().getRadarImage(modelx - w, modely - h, w * 3, h * 3);
		if (im != null) {
			g.drawImage(im, x, y, rw, rh, null);
		} else {
			g.drawString("no radar", x + 50, y + 50);
		}
		
		// TODO now paint radar objects...
		
		g.drawRect(x, y, rw, rh);
	}
	
	@Override
	public String toString() {
		return String.format("View[%d,%d]", getWidth(), getHeight());
	}
	
}


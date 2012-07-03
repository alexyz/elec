
package el;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.*;

import el.serv.ServerMain;

/**
 * Application frame and main method
 */
public class ClientFrame extends JFrame {
	
	public static final String SHIP1_IMAGE = "/img/ship1.png";
	public static final String TILE1_IMAGE = "/img/tile1.png";
	
	private static final Map<String, Image> images = new HashMap<String, Image>();
	
	private static ClientFrame frame;
	
	public static ClientFrame getInstance() {
		return frame;
	}
	
	//
	// instance stuff
	//
	
	/** render time / timer duration ratio */
	public float busy;
	/** anti aliasing enabled */
	public boolean aa = true;
	/** map grid lines */
	public boolean grid;
	
	private final Model model = new Model();
	private final CanvasView view = new CanvasView(model);
	
	/** view repaint timer */
	private Timer timer;
	/** server connection */
	private ServerRunnable server;
	
	public static void main(String[] args) {
		System.out.println("user.dir: " + System.getProperty("user.dir"));
		// this should be false, looks terrible
		System.out.println("sun.java2d.opengl: " + System.getProperty("sun.java2d.opengl"));
		frame = new ClientFrame();
		frame.setVisible(true);
	}
	
	public ClientFrame() {
		setTitle("Electron");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setJMenuBar(createMenuBar());
		
		final JPanel p = new JPanel(new BorderLayout());
		p.add(view, BorderLayout.CENTER);
		setContentPane(p);
		pack();
		
		timer = new Timer(25, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				long startt = System.nanoTime();
				
				try {
					// don't need to sync as server updates are on AWT thread
					model.update();
					// should skip this if behind
					// or just reduce frame rate
					view.paintImmediately(0, 0, view.getWidth(), view.getHeight());
					
				} catch (Exception ex) {
					ex.printStackTrace(System.out);
					ClientFrame.handleException("Timer", ex);
				}
				
				long endt = System.nanoTime();
				long renderTime = endt - startt;
				busy = (renderTime + 0f) / (timer.getDelay() * 1000000f); 
			}
		});
		
		timer.start();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// do initial paint and print some information
				try {
					System.out.println("view request focus: " + view.requestFocusInWindow());
					view.paintImmediately(0, 0, view.getWidth(), view.getHeight());
					System.out.println("panel double buffered: " + p.isDoubleBuffered());
					System.out.println("panel opaque: " + p.isOpaque());
					System.out.println("panel optimized: " + p.isOptimizedDrawingEnabled());
					System.out.println("panel lightweight: " + p.isLightweight());
					System.out.println("view double buffered: " + view.isDoubleBuffered());
					System.out.println("view opaque: " + view.isOpaque());
					BufferStrategy bs = view.getBufferStrategy();
					System.out.println("canvas view buffer strategy: " + bs.getClass());
					BufferCapabilities c = bs.getCapabilities();
					System.out.println("canvas view buffer full screen required: " + c.isFullScreenRequired());
					System.out.println("canvas view buffer multi buffer: " + c.isMultiBufferAvailable());
					System.out.println("canvas view buffer page flipping: " + c.isPageFlipping());
					System.out.println("canvas view buffer back buffer accel: " + c.getBackBufferCapabilities().isAccelerated());
					System.out.println("canvas view buffer front buffer accel: " + c.getFrontBufferCapabilities().isAccelerated());
					
					// type 0 is custom, 1 is INT_RGB
					System.out.println("static image: " + createImageA(100,100));
					System.out.println("frame image: " + createImage(100,100));
					System.out.println("canvas image: " + view.createImage(100,100));
					
				} catch (Exception e) {
					e.printStackTrace(System.out);
					handleException("Init", e);
				}
			}
		});
		
	}
	
	/**
	 * display dialog and exit
	 */
	public static void handleException(String title, Exception e) {
		JOptionPane.showMessageDialog(ClientFrame.frame, 
				e.toString(), // + ": " + e.getMessage(), 
				title, 
				JOptionPane.ERROR_MESSAGE);
		System.exit(-1);
	}
	
	public void disconnect() {
		JOptionPane.showMessageDialog(frame, "Disconnected", "Disconnect", JOptionPane.WARNING_MESSAGE);
		model.setServer(null);
		server = null;
	}
	
	private JMenuBar createMenuBar() {
		
		JMenuItem createServerMenuItem = new JMenuItem("Start local server");
		createServerMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ServerMain server = new ServerMain(8111);
					Thread t = new Thread(server, "ServerThread");
					t.setPriority(Thread.NORM_PRIORITY);
					t.setDaemon(true);
					t.start();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame, ex.toString());
				}
			}
		});
		
		JMenuItem connectMenuItem = new JMenuItem("Connect to local server");
		connectMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (server != null) {
						JOptionPane.showMessageDialog(frame, "Already connected", "Connect", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					String name = JOptionPane.showInputDialog(frame, "Enter name", "Connect", JOptionPane.QUESTION_MESSAGE);
					if (name == null || name.length() == 0) {
						return;
					}
					
					Socket socket = new Socket("localhost", 8111);
					ServerRunnable server = new ServerRunnable(socket, model, name);
					
					// should create new model and view at this point
					model.setServer(server);
					ClientFrame.this.server = server;
					frame.setTitle("Electron - " + name);
					
					// start - sends name and starts listening
					Thread t = new Thread(server, "ServerThread");
					t.setDaemon(true);
					t.setPriority(Thread.NORM_PRIORITY);
					t.start();
					
					// focus playing area
					view.requestFocusInWindow();
					
				} catch (Exception ex) {
					handleException("Connect", ex);
				}
			}
		});
		
		JMenuItem enterReqMenuItem = new JMenuItem("Enter");
		enterReqMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (server != null) {
					server.getServerProxy().enterReq();
				} else {
					JOptionPane.showMessageDialog(frame, "Not connected");
				}
				// have to request focus on view otherwise menu bar keeps focus
				view.requestFocusInWindow();
			}
		});
		
		JMenuItem specMenuItem = new JMenuItem("Spectate");
		specMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (server != null) {
					server.getServerProxy().spec();
					// wait for server?
					//model.spec();
				} else {
					JOptionPane.showMessageDialog(frame, "Not connected");
				}
				view.requestFocusInWindow();
			}
		});
		
		JMenuItem pingMenuItem = new JMenuItem("Ping");
		pingMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (server != null) {
					model.ping();
				} else {
					JOptionPane.showMessageDialog(frame, "Not connected");
				}
				view.requestFocusInWindow();
			}
		});
		
		final JMenuItem consoleMenuItem = new JMenuItem("Console");
		consoleMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ConsoleFrame.getInstance().setVisible(true);
			}
		});
		
		final JCheckBoxMenuItem aaMenuItem = new JCheckBoxMenuItem("Anti-alias");
		aaMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				aa = aaMenuItem.isSelected();
			}
		});
		aaMenuItem.setSelected(aa);
		
		final JMenuItem aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(frame, "http://github.com/alexyz/elec");
			}
		});
		
		//
		
		JMenu serverMenu = new JMenu("Server");
		serverMenu.add(createServerMenuItem);
		serverMenu.add(connectMenuItem);
		serverMenu.add(enterReqMenuItem);
		serverMenu.add(specMenuItem);
		serverMenu.add(pingMenuItem);
		
		JMenu localMenu = new JMenu("Local");
		localMenu.add(consoleMenuItem);
		localMenu.add(aaMenuItem);
		localMenu.add(aboutMenuItem);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(serverMenu);
		menuBar.add(localMenu);
		return menuBar;
	}
	
	/**
	 * Reduce frame rate
	 */
	public void setDelay(int delay) {
		timer.setDelay(delay);
	}
	
	/**
	 * Get delay between frames
	 */
	public int getDelay() {
		return timer.getDelay();
	}
	
	/**
	 * Create image suitable for rendering
	 */
	public static BufferedImage createImageA(int w, int h) {
		// JFrame.createImage create image without alpha transparency, which isn't useful
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		BufferedImage image = gc.createCompatibleImage(w, h, Transparency.BITMASK);
		return image;
	}
	
	/**
	 * Get cached image
	 */
	public static synchronized Image getImage(String name) {
		Image image = images.get(name);
		if (image == null) {
			URL u = ClientFrame.class.getResource(name);
			System.out.println("loading image " + u);
			try {
				BufferedImage i = ImageIO.read(u);
				image = createImageA(i.getWidth(), i.getHeight());
				Graphics g = image.getGraphics();
				g.drawImage(i, 0, 0, null);
				g.dispose();
				images.put(name, image);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return image;
	}
}

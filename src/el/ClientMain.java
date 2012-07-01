
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

// create frame, view and model
// should probably be subclass of JFrame and have getInstance method
public class ClientMain {
	
	public static final String SHIP1_IMAGE = "/img/ship1.png";
	public static final String TILE1_IMAGE = "/img/tile1.png";
	
	// global information
	public static float busy;
	public static boolean aa = true;
	public static boolean grid;
	
	private static final PrintStream out = System.out;
	private static final Map<String, Image> images = new HashMap<String, Image>();
	private static final Model model = new Model();
	private static final CanvasView view = new CanvasView(model);
	
	private static JFrame frame;
	private static Timer timer;
	private static ServerRunnable server;
	
	public static void main(String[] args) {
		
		out.println("dir: " + System.getProperty("user.dir"));
		
		final JPanel p = new JPanel(new BorderLayout());
		p.add(view, BorderLayout.CENTER);
		
		JFrame f = new JFrame();
		f.setTitle("Electron");
		// TODO OS X doesn't close connection, should do manually
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setJMenuBar(createMenuBar());
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);
		
		frame = f;
		
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
					ex.printStackTrace(out);
					ClientMain.handleException("Timer", ex);
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
				} catch (Exception e) {
					e.printStackTrace(out);
					handleException("Init", e);
				}
			}
		});
		
	}
	
	/**
	 * display dialog and exit
	 */
	public static void handleException(String title, Exception e) {
		JOptionPane.showMessageDialog(ClientMain.frame, 
				e.toString(), // + ": " + e.getMessage(), 
				title, 
				JOptionPane.ERROR_MESSAGE);
		System.exit(-1);
	}
	
	public static void disconnect() {
		JOptionPane.showMessageDialog(frame, "Disconnected", "Disconnect", JOptionPane.WARNING_MESSAGE);
		model.setServer(null);
		server = null;
	}
	
	private static JMenuBar createMenuBar() {
		JMenuItem connectMenuItem = new JMenuItem("Connect (localhost/8111)");
		connectMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (server != null) {
						JOptionPane.showMessageDialog(frame, "Already connected", "Connect", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					String name = JOptionPane.showInputDialog(frame, "Enter name", "Connect", JOptionPane.QUESTION_MESSAGE);
					if (name.length() == 0) {
						return;
					}
					
					Socket socket = new Socket("localhost", 8111);
					ServerRunnable server = new ServerRunnable(socket, model, name);
					
					// should create new model and view at this point
					model.setServer(server);
					ClientMain.server = server;
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
		
		JMenu serverMenu = new JMenu("Server");
		serverMenu.add(connectMenuItem);
		serverMenu.add(enterReqMenuItem);
		serverMenu.add(specMenuItem);
		serverMenu.add(pingMenuItem);
		
		final JCheckBoxMenuItem gridMenuItem = new JCheckBoxMenuItem("Map Grid");
		gridMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				grid = gridMenuItem.isSelected();
			}
		});
		gridMenuItem.setSelected(grid);
		
		final JCheckBoxMenuItem aaMenuItem = new JCheckBoxMenuItem("Anti-alias");
		aaMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				aa = aaMenuItem.isSelected();
			}
		});
		aaMenuItem.setSelected(aa);
		
		JMenu localMenu = new JMenu("Local");
		localMenu.add(gridMenuItem);
		localMenu.add(aaMenuItem);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(serverMenu);
		menuBar.add(localMenu);
		return menuBar;
	}
	
	/**
	 * Reduce frame rate
	 */
	public static void setDelay(int delay) {
		timer.setDelay(delay);
	}
	
	/**
	 * Get delay between frames
	 */
	public static int getDelay() {
		return timer.getDelay();
	}
	
	/**
	 * Get cached image
	 */
	public static Image getImage(String name) {
		Image image = images.get(name);
		if (image == null) {
			URL u = ClientMain.class.getResource(name);
			System.out.println("loading image " + u);
			try {
				BufferedImage i = ImageIO.read(u);
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				GraphicsDevice gd = ge.getDefaultScreenDevice();
				GraphicsConfiguration gc = gd.getDefaultConfiguration();
				image = gc.createCompatibleImage(i.getWidth(), i.getHeight(), Transparency.BITMASK);
				image.getGraphics().drawImage(i, 0, 0, null);
				images.put(name, image);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return image;
	}
}

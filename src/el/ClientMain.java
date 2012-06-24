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

public class ClientMain {
	static final PrintStream out = System.out;
	private static final Map<String,Image> images = new HashMap<String,Image>();
	static Timer timer;
	
	public static void main(String[] args) {
		
		out.println("dir: " + System.getProperty("user.dir"));
		
		final Model m = new Model();
		//final JCView v = new JCView(m);
		final CanvasView v = new CanvasView(m);
		
		final JPanel p = new JPanel(new BorderLayout());
		p.add(v, BorderLayout.CENTER);
		
		JMenuItem connectMenuItem = new JMenuItem("Connect");
		connectMenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Socket s = new Socket("localhost", 8111);
					ServerThread st = new ServerThread(s, m);
					st.start();
					m.setServerThread(st);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		JMenu networkMenu = new JMenu("Network");
		networkMenu.add(connectMenuItem);
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(networkMenu);
		
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//f.setJMenuBar(menuBar);
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// do initial paint and print some information
				v.paintImmediately(0, 0, v.getWidth(), v.getHeight());
				System.out.println("panel double buffered: " + p.isDoubleBuffered());
				System.out.println("panel opaque: " + p.isOpaque());
				System.out.println("panel optimized: " + p.isOptimizedDrawingEnabled());
				System.out.println("panel lightweight: " + p.isLightweight());
				System.out.println("view double buffered: " + v.isDoubleBuffered());
				System.out.println("view opaque: " + v.isOpaque());
				BufferStrategy bs = v.getBufferStrategy();
				System.out.println("canvas view buffer strategy: " + bs.getClass());
				BufferCapabilities c = bs.getCapabilities();
				System.out.println("canvas view buffer full screen required: " + c.isFullScreenRequired());
				System.out.println("canvas view buffer multi buffer: " + c.isMultiBufferAvailable());
				System.out.println("canvas view buffer page flipping: " + c.isPageFlipping());
				System.out.println("canvas view buffer back buffer accel: " + c.getBackBufferCapabilities().isAccelerated());
				System.out.println("canvas view buffer front buffer accel: " + c.getFrontBufferCapabilities().isAccelerated());
			}
		});
		
		timer = new Timer(25, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m.update();
				// should skip this if behind
				v.paintImmediately(0, 0, v.getWidth(), v.getHeight());
			}
		});
		timer.start();
	}
	static void faster() {
		timer.setDelay(timer.getDelay() - 1);
	}
	static void slower() {
		timer.setDelay(timer.getDelay() + 1);
	}
	static int delay() {
		return timer.getDelay();
	}
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
				image = gc.createCompatibleImage(i.getWidth(),i.getHeight(),Transparency.BITMASK);
				image.getGraphics().drawImage(i,0,0,null);
				images.put(name, image);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return image;
	}
}




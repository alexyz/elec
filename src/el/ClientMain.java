package el;
import java.awt.*;
import java.awt.event.*;
import java.io.PrintStream;
import java.net.Socket;

import javax.swing.*;

public class ClientMain {
	static final PrintStream out = System.out;
	static Timer timer;
	
	public static void main(String[] args) {
		final Model m = new Model();
		final View v = new View(m);
		
		JPanel p = new JPanel(new BorderLayout());
		System.out.println("panel double buffered: " + p.isDoubleBuffered());
		System.out.println("panel opaque: " + p.isOpaque());
		System.out.println("panel optimized: " + p.isOptimizedDrawingEnabled());
		System.out.println("panel lightweight: " + p.isLightweight());
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
		f.setJMenuBar(menuBar);
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);
		
		timer = new Timer(50, new ActionListener() {
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
}




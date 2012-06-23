package el;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.*;

public class Main {
	static final PrintStream out = System.out;
	static Timer t;
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
		
		t = new Timer(50, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m.update();
				// should skip this if behind
				v.paintImmediately(0, 0, v.getWidth(), v.getHeight());
			}
		});
		t.start();
	}
	static void faster() {
		t.setDelay(t.getDelay() - 1);
	}
	static void slower() {
		t.setDelay(t.getDelay() + 1);
	}
	static int delay() {
		return t.getDelay();
	}
}




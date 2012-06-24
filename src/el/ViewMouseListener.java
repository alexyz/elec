package el;

import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

class ViewMouseListener extends MouseInputAdapter {
	public void mouseClicked(MouseEvent e) {
		System.out.println("mouse clicked at " + e.getPoint());
	}
	public void mouseDragged(MouseEvent e) {
		//
	}
}
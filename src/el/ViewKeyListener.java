package el;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * view keyboard listener and message buffer
 */
class ViewKeyListener implements KeyListener {
	
	public final StringBuilder buf = new StringBuilder();
	
	private final Model model;
	
	public ViewKeyListener(Model model) {
		this.model = model;
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		//System.out.println("typed '" + e.getKeyChar() + "'");
		ClientFrame f = ClientFrame.getInstance();
		char c = e.getKeyChar();
		switch (c) {
			case '¤':
			case '`':
				model.focusCycle();
				break;
			case '[':
				f.setDelay(f.getDelay() + 1);
				break;
			case ']':
				f.setDelay(f.getDelay() - 1);
				break;
			case '\b':
				if (buf.length() > 0) {
					buf.delete(buf.length() - 1, buf.length());
				}
				break;
			case '\n':
			case '\r':
				if (buf.length() > 0) {
					model.sendMsg(buf.toString());
					buf.delete(0, buf.length());
				}
				break;
			default:
				if (c >= 32) {
					buf.append(c);
				}
				break;
				// System.out.printf("typed '%c' -> %d\n", e.getKeyChar(),
				// e.getKeyCode());
		}
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
		int c = e.getKeyCode();
		//System.out.println("pressed " + c);
		switch (c) {
			case KeyEvent.VK_UP:
				// needs to set isUp
				// or have some queue of actions in the model
				model.action(ActionMap.UP);
				break;
			case KeyEvent.VK_DOWN:
				model.action(ActionMap.DOWN);
				break;
			case KeyEvent.VK_LEFT:
				model.action(ActionMap.LEFT);
				break;
			case KeyEvent.VK_RIGHT:
				model.action(ActionMap.RIGHT);
				break;
			case KeyEvent.VK_CONTROL:
				model.action(ActionMap.FIRE1);
				break;
			case KeyEvent.VK_TAB:
				if (e.isShiftDown()) {
					model.action(ActionMap.FIRE3);
				} else {
					model.action(ActionMap.FIRE2);
				}
				break;
			default:
				//System.out.printf("pressed '%c' -> %d\n", e.getKeyChar(), e.getKeyCode());
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				model.unaction(ActionMap.UP);
				break;
			case KeyEvent.VK_DOWN:
				model.unaction(ActionMap.DOWN);
				break;
			case KeyEvent.VK_LEFT:
				model.unaction(ActionMap.LEFT);
				break;
			case KeyEvent.VK_RIGHT:
				model.unaction(ActionMap.RIGHT);
				break;
			case KeyEvent.VK_CONTROL:
				model.unaction(ActionMap.FIRE1);
				break;
			case KeyEvent.VK_TAB:
				model.unaction(ActionMap.FIRE2);
				model.unaction(ActionMap.FIRE3);
				break;
			case KeyEvent.VK_ESCAPE:
				System.exit(0);
				break;
			default:
				// System.out.printf("released '%c' -> %d\n", e.getKeyChar(),
				// e.getKeyCode());
		}
	}
	
}

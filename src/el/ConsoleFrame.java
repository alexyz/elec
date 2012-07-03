package el;

import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

/**
 * A text area in a frame that replaces System.out when it is set visible.
 */
public class ConsoleFrame extends JFrame {
	
	private static final PrintStream oldOut = System.out;
	private static ConsoleFrame console;
	private final JTextArea area;
	private final JScrollPane scroll;
	
	public static ConsoleFrame getInstance() {
		if (console == null) {
			console = new ConsoleFrame();
		}
		return console;
	}

	private ConsoleFrame() {
		super("Console");
		area = new JTextArea();
		area.setFont(new Font("Monospaced", Font.PLAIN, 10));
		area.setLineWrap(true);
		area.setEditable(false);
		scroll = new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setPreferredSize(new Dimension(640, 480));
		setContentPane(scroll);
		pack();
	}
	
	@Override
	public void setVisible(boolean v) {
		super.setVisible(v);
		if (v) {
			oldOut.println("-- enable java console --");
			System.setOut(new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					oldOut.write(b);
					write(new byte[] { (byte) b });
				}
				@Override
				public void write(byte[] b) throws IOException {
					oldOut.write(b);
					write(b, 0, b.length);
				}
				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					oldOut.write(b, off, len);
					// create new string before pushing on queue
					final String s = new String(b, off, len);
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							if (area.getDocument().getLength() > 100000) {
								try {
									area.getDocument().remove(0, 50000);
								} catch (BadLocationException e) {
									e.printStackTrace(oldOut);
								}
							}
							area.append(s);
							area.setCaretPosition(area.getDocument().getLength());
						}
					});
				}
			}));
		} else {
			oldOut.println("-- disable java console --");
			System.setOut(oldOut);
		}
	}
}

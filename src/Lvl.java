
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

/**
 * small program to read subspace lvl files and write the tileset to a bmp and
 * the map data to a png
 */
public class Lvl {
	public static void main(String[] args) throws Exception {
		FileInputStream is = new FileInputStream(args[0]);
		FileOutputStream os1 = new FileOutputStream(args[0] + ".bmp");
		byte[] buf = new byte[0xc236];
		is.read(buf);
		os1.write(buf);
		os1.close();
		System.out.println("wrote bmp");
		byte[][] map = new byte[1024][1024];
		byte[] w = new byte[4];
		while (is.available() > 0) {
			is.read(w);
			int i = w[3] & 0xff;
			i = (i << 8) | (w[2] & 0xff);
			i = (i << 8) | (w[1] & 0xff);
			i = (i << 8) | (w[0] & 0xff);
			int t = i >>> 24;
			int y = (i >>> 12) & 0x03FF;
			int x = i & 0x03FF;
			// System.out.println("x " + x + " y " + y + " t " + t);
			map[x][y] = (byte) t;
		}
		is.close();
		BufferedImage im = new BufferedImage(1024, 1024, BufferedImage.TYPE_BYTE_GRAY);
		for (int x = 0; x < 1024; x++) {
			for (int y = 0; y < 1024; y++) {
				int b = ~map[x][y] & 0xff;
				im.setRGB(x, y, b | (b << 8) | (b << 16));
			}
		}
		ImageIO.write(im, "png", new File(args[0] + ".png"));
		System.out.println("wrote png");
	}
}

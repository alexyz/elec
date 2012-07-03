
package el.bg;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import el.ClientFrame;

/**
 * small program to read subspace lvl files and write the tileset and
 * the map data to png files.
 * see http://www.rarefied.org/subspace/lvlformat.html
 */
public class Lvl {
	
	public static BufferedImage[] getTileImages(BufferedImage tilesImage) {
		// 304,160 16*6
		if (tilesImage.getWidth() != 304 || tilesImage.getHeight() != 160) {
			throw new RuntimeException("not a subspace tileset");
		}
		BufferedImage[] tileImages = new BufferedImage[190];
		int n = 0;
		for (int y = 0; y < 160; y+=16) {
			for (int x = 0; x < 304; x+=16) {
				// get image for graphics device
				// XXX getinstance?
				BufferedImage im = ClientFrame.createImageA(16, 16);
				Graphics g = im.getGraphics();
				g.drawImage(tilesImage, -x, -y, null);
				tileImages[n++] = im;
			}
		}
		System.out.println("loaded tile images");
		return tileImages;
	}
	
	public static byte[][] getMapArray(BufferedImage mapImage) {
		int w = mapImage.getWidth();
		int h = mapImage.getHeight();
		byte[][] m = new byte[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				// get it from blue channel only
				int val = mapImage.getRGB(x, y) & 0xff;
				m[x][y] = (byte) val;
			}
		}
		System.out.println("loaded map array");
		return m;
	}
	
	public static void main(String[] args) throws Exception {
		FileInputStream is = new FileInputStream(args[0]);
		
		// read bmp data
		byte[] buf = new byte[0xc236];
		is.read(buf);
		
		BufferedImage tim = ImageIO.read(new ByteArrayInputStream(buf));
		ImageIO.write(tim, "png", new File(args[0] + ".tiles.png"));
		System.out.println("wrote tiles png");
		
		byte[][] map = new byte[1024][1024];
		while (is.available() > 0) {
			int i = is.read();
			i |= (is.read() << 8);
			i |= (is.read() << 16);
			i |= (is.read() << 24);
			
			int t = i >>> 24;
			int y = (i >>> 12) & 0x3ff;
			int x = i & 0x3ff;
			// System.out.println("x " + x + " y " + y + " t " + t);
			if (map[x][y] != 0) {
				System.out.println("overwriting " + x + "," + y);
			}
			map[x][y] = (byte) t;
		}
		is.close();
		
		BufferedImage im = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < 1024; x++) {
			for (int y = 0; y < 1024; y++) {
				int b = map[x][y] & 0xff;
				im.setRGB(x, y, b | (b << 8) | (b << 16));
			}
		}
		ImageIO.write(im, "png", new File(args[0] + ".map.png"));
		System.out.println("wrote map png");
	}
}

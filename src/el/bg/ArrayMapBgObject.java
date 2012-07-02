package el.bg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;

import el.Model;
import el.phys.Circle;
import el.phys.FloatMath;
import el.phys.Intersection;
import el.phys.Rect;
import el.phys.Trans;
import el.phys.cs.CSIntersect;


public class ArrayMapBgObject extends MapBgObject {
	
	private byte[][] mapArray = new byte[0][0];
	private BufferedImage[] tileImages;
	/** position of map in model */
	private int mapx, mapy;
	private String mapName, tilesName;
	
	public ArrayMapBgObject() {
		// 
	}
	
	/**
	 * load map data from given files
	 */
	@Override
	public void read(String data) {
		try {
			// map.png tiles.png
			StringTokenizer tokens = new StringTokenizer(data);
			String mapName = tokens.nextToken();
			File mapFile = new File(mapName);
			System.out.println("Map file " + mapFile + " exists: " + mapFile.exists());
			
			String tilesName = tokens.nextToken();
			File tilesFile = new File(tilesName);
			System.out.println("Tiles file " + tilesFile + " exists: " + tilesFile.exists());
			
			BufferedImage mapImage = ImageIO.read(mapFile);
			BufferedImage tilesImage = ImageIO.read(tilesFile);
			
			byte[][] mapArray = Lvl.getMapArray(mapImage);
			BufferedImage[] tileImages = Lvl.getTileImages(tilesImage);
			
			this.mapName = mapName;
			this.tilesName = tilesName;
			this.mapArray = mapArray;
			this.tileImages = tileImages;
			this.mapx = Model.centrex - ((mapArray.length * 16) / 2);
			this.mapy = Model.centrey - ((mapArray[0].length * 16) / 2);
			
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}
	}
	
	@Override
	public String write() {
		return mapName + " " + tilesName;
	}
	
	@Override
	public void paint(Graphics2D g, float modelx_, float modely_) {
		int w = g.getClipBounds().width, h = g.getClipBounds().height;
		if (mapArray.length == 0) {
			g.setColor(Color.white);
			g.drawString("no map data", w / 2, h / 2);
			return;
		}
		
		// convert model to int
		int modelx = (int) modelx_;
		int modely = (int) modely_;
		
		// get tile number of tile overlapping top left of screen (could be negative)
		int xotile = (modelx - mapx) / 16;
		int yotile = (modely - mapy) / 16;
		
		g.setColor(Color.gray);
		g.drawString("x,y=" + xotile + "," + yotile, 5, 100);
		
		// get the x,y between origin of that tile and origin of screen
		int xtileoff = (modelx - mapx) % 16;
		int ytileoff = (modely - mapy) % 16;
		
		// how many tiles to display on screen
		int tilesw = (w / 16) + 1;
		int tilesh = (h / 16) + 1;
		
		// for each tile on screen
		for (int x = 0; x < tilesw; x++) {
			for (int y = 0; y < tilesh; y++) {
				// is it a valid tile
				int xt = xotile + x;
				int yt = yotile + y;
				if (xt >= 0 && xt < mapArray.length && yt > 0 && yt < mapArray[0].length) {
					// is it a non zero tile
					int b = mapArray[xt][yt] & 0xff;
					if (b != 0) {
						// get screen x,y of tile
						int sx = x * 16 - xtileoff;
						int sy = y * 16 - ytileoff;
						// draw it
						if (b < tileImages.length) {
							g.drawImage(tileImages[b - 1], sx, sy, null);
						} else {
							g.drawRect(sx, sy, 16, 16);
							g.drawString(Integer.toHexString(b & 0xff), sx, sy + 16);
						}
					}
				}
			}
		}
	}
	
	@Override
	public Intersection intersects(Circle c, float ctx, float cty) {
		// loop over all map tiles in dest square
		// check for ints. with each and keep on with lowest hypot
		
		// get top left of trans square
		int rx = (int) (c.x + ctx - c.radius);
		int ry = (int) (c.y + cty - c.radius);
		
		// get tile number of tile overlapping top left of dest square (could be negative)
		int xotile = (rx - mapx) / 16;
		int yotile = (ry - mapy) / 16;
		
		// how many tiles to display on screen
		int tilesw = (int) ((c.radius * 2) / 16) + 1;
		
		Intersection i = null;
		float minHypot = Float.MAX_VALUE;
		
		// for each tile on screen
		for (int x = 0; x < tilesw; x++) {
			for (int y = 0; y < tilesw; y++) {
				// is it a valid tile
				int xt = xotile + x;
				int yt = yotile + y;
				if (xt >= 0 && xt < mapArray.length && yt > 0 && yt <= mapArray[0].length) {
					int b = mapArray[xt][yt] & 0xff;
					if (b != 0) {
						//System.out.println(
						int mtx = mapx + ((xotile + x) * 16);
						int mty = mapy + ((yotile + y) * 16);
						
						float hypot = FloatMath.hypot(rx - mtx, ry - mty);
						if (hypot < minHypot) {
							Rect r = new Rect(mtx, mty, mtx + 16, mty + 16);
							System.out.println("map tile: " + r + " ship: " + r + " t=" + ctx + "," + cty);
							i = CSIntersect.intersect(r, c, ctx, cty, 0.95f);
							System.out.println("collision! " + i);
						}
					}
				}
			}
		}
		return i;
	}
	
	
}

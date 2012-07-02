package el.bg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import el.Model;
import el.fg.TransObject;
import el.phys.Circle;
import el.phys.FloatMath;
import el.phys.Intersection;
import el.phys.Rect;
import el.phys.cs.CSIntersect;


public class ArrayMapBgObject extends MapBgObject {
	
	private byte[][] mapArray = new byte[0][0];
	private BufferedImage[] tileImages;
	/** position of map in model */
	private int mapModelX, mapModelY;
	private String mapName, tilesName;
	private final Set<Integer> annoTiles = new TreeSet<Integer>();
	
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
			this.mapModelX = Model.centrex - ((mapArray.length * 16) / 2);
			this.mapModelY = Model.centrey - ((mapArray[0].length * 16) / 2);
			
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
		int xotile = (modelx - mapModelX) / 16;
		int yotile = (modely - mapModelY) / 16;
		
		g.setColor(Color.gray);
		g.drawString("x,y=" + xotile + "," + yotile, 5, 100);
		
		// get the x,y between origin of that tile and origin of screen
		int xtileoff = (modelx - mapModelX) % 16;
		int ytileoff = (modely - mapModelY) % 16;
		
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
				
				/*
				if (annoTiles.contains((xt << 16) + yt)) {
					int sx = x * 16 - xtileoff;
					int sy = y * 16 - ytileoff;
					g.setColor(Color.yellow);
					g.drawRect(sx, sy, 16, 16);
				}
				*/
			}
		}
	}
	
	float lastUpdate;
	
	@Override
	public void update(float t, float dt) {
		if (t > lastUpdate + 0.5) {
			lastUpdate = t;
			annoTiles.clear();
		}
	}
	
	@Override
	public Intersection intersects(Circle c, float ctx, float cty) {
		// get top left of translated square
		int rx = (int) (c.x + ctx - c.radius);
		int ry = (int) (c.y + cty - c.radius);
		
		// get tile number of tile overlapping top left of dest square (could be negative)
		int xotile = (rx - mapModelX) / 16;
		int yotile = (ry - mapModelY) / 16;
		
		// how many tiles to display on screen
		// have to do +2 here because int cast rounds down and div rounds down (?)
		int tilesw = (int) ((c.radius * 2) / 16) + 2;
		
		Intersection i = null;
		
		// for each tile on screen
		for (int x = 0; x < tilesw; x++) {
			for (int y = 0; y < tilesw; y++) {
				// is it a valid tile
				int xt = xotile + x;
				int yt = yotile + y;
				if (xt >= 0 && xt < mapArray.length && yt > 0 && yt < mapArray[0].length) {
					
					int b = mapArray[xt][yt] & 0xff;
					if (b != 0) {
						int mtx = mapModelX + ((xotile + x) * 16);
						int mty = mapModelY + ((yotile + y) * 16);
						
						Rect r = new Rect(mtx, mty, mtx + 16, mty + 16);
						Intersection i2 = CSIntersect.intersect(r, c, ctx, cty, 0.95f);
						if (i2 != null) {
							//System.out.println("is: rect " + r + " circ " + c + " t " + ctx + "," + cty);
							if (i != null) {
								// two intersections (from different tiles) to choose from, pick one with lowest param
								if (i2.p < i.p) {
									i = i2;
								} else if (i2.p == i.p) {
									// great, two intersections with same param
									// stick with first one, though the reflected point might be within another tile
									System.out.println("equal intersect i=" + i + " i2=" + i2);
								}
							} else {
								i = i2;
							}
							
						} else {
							// did not find exception
							annoTiles.add((xt << 16) + yt);
						}
					}
				}
			}
		}
		// XXX may need to reflect reflection...
		return i;
	}
	
	
}

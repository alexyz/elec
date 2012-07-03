package el.bg;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import el.ClientFrame;
import el.Model;
import el.phys.Circle;
import el.phys.Intersection;
import el.phys.Rect;
import el.phys.cs.CSIntersect;

/**
 * A subspace-style map that stores the maps tiles in an array
 */
public class ArrayMapBgObject extends MapBgObject {
	
	private static final int tilesz = 16;
	
	/** the map data, never null */
	private byte[][] mapArray = new byte[0][0];
	/** the map tile images, possibly null */
	private BufferedImage[] tileImages;
	/** position of map in model */
	private int mapModelX, mapModelY;
	/** map and tiles file name */
	private String mapName, tilesName;
	/** debugging - annotated tiles */
	private final Set<Integer> annoTiles = new TreeSet<Integer>();
	/** map image for radar */
	private BufferedImage mapImage;
	/** time of last call to update */
	private float lastUpdate;
	
	public ArrayMapBgObject() {
		//
	}
	
	@Override
	public BufferedImage getRadarImage(float modelx_, float modely_, float modelw, float modelh) {
		if (mapImage == null) {
			return null;
		}
		
		// convert model to tile, equiv to pixel as there is 1:1 mapping in image
		int x = (int) ((modelx_ - mapModelX) / tilesz);
		int y = (int) ((modely_ - mapModelY) / tilesz); 
		int w = (int) (modelw / tilesz);
		int h = (int) (modelh / tilesz);
		
		// hack
		x = Math.max(x, 0);
		y = Math.max(y, 0);
		w = Math.min(w, mapImage.getWidth());
		h = Math.min(h, mapImage.getHeight());

		return mapImage.getSubimage(x, y, w, h);
	}
	
	/**
	 * load map data from given files
	 */
	@Override
	public void read(String data) {
		try {
			// "map.png tiles.png"
			StringTokenizer tokens = new StringTokenizer(data);
			String mapName = tokens.nextToken();
			URL mapUrl = getClass().getResource(mapName);
			System.out.println("Map file " + mapUrl);
			String tilesName = tokens.nextToken();
			URL tilesUrl = getClass().getResource(tilesName);
			System.out.println("Tiles file " + tilesUrl);
			
			BufferedImage mapImage = ImageIO.read(mapUrl);
			BufferedImage tilesImage = ImageIO.read(tilesUrl);
			
			byte[][] mapArray = Lvl.getMapArray(mapImage);
			BufferedImage[] tileImages = Lvl.getTileImages(tilesImage);
			
			// convert map image to native
			BufferedImage mapImage2 = ClientFrame.createImageA(mapImage.getWidth(), mapImage.getHeight());
			Graphics2D g = mapImage2.createGraphics();
			g.drawImage(mapImage, 0, 0, null);
			g.dispose();
			// improve contrast of map image for radar
			RescaleOp op = new RescaleOp(2f, 32f, null);
			op.filter(mapImage2, mapImage2);
			
			// set variables at end in case there were any exceptions
			this.mapName = mapName;
			this.tilesName = tilesName;
			this.mapArray = mapArray;
			this.tileImages = tileImages;
			this.mapModelX = Model.centrex - ((mapArray.length * tilesz) / 2);
			this.mapModelY = Model.centrey - ((mapArray[0].length * tilesz) / 2);
			this.mapImage = mapImage2;
			
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
			g.setColor(Color.gray);
			g.drawString("no map data", w / 2, h / 2);
			return;
		}
		
		// convert model to int
		int modelx = (int) modelx_;
		int modely = (int) modely_;
		
		// get tile number of tile overlapping top left of screen (could be negative)
		int xotile = (modelx - mapModelX) / tilesz;
		int yotile = (modely - mapModelY) / tilesz;
		
		g.setColor(Color.gray);
		g.drawString("x,y=" + xotile + "," + yotile, 5, 100);
		
		// get the x,y between origin of that tile and origin of screen
		int xtileoff = (modelx - mapModelX) % tilesz;
		int ytileoff = (modely - mapModelY) % tilesz;
		
		// how many tiles to display on screen
		int tilesw = (w / tilesz) + 1;
		int tilesh = (h / tilesz) + 1;
		
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
						int sx = x * tilesz - xtileoff;
						int sy = y * tilesz - ytileoff;
						// draw it
						if (b < tileImages.length) {
							g.drawImage(tileImages[b - 1], sx, sy, null);
						} else {
							g.drawRect(sx, sy, tilesz, tilesz);
							g.drawString(Integer.toHexString(b & 0xff), sx, sy + tilesz);
						}
					}
				}
				
				// creates loads of garbage, should iterate over map instead
				/*
				if (annoTiles.contains((xt << 16) + yt)) {
					int sx = x * tilesz - xtileoff;
					int sy = y * tilesz - ytileoff;
					g.setColor(Color.yellow);
					g.drawRect(sx, sy, tilesz, tilesz);
				}
				 */
			}
		}
	}
	
	@Override
	public void update(float t, float dt) {
		if (t > lastUpdate + 0.5) {
			lastUpdate = t;
			annoTiles.clear();
		}
	}
	
	@Override
	public Intersection intersects(Circle c, float ctx, float cty) {
		// get top left of translated square - rounds down
		int rx = (int) (c.x + ctx - c.radius);
		int ry = (int) (c.y + cty - c.radius);
		
		// get tile number of tile overlapping top left of dest square (could be negative)
		int xotile = (rx - mapModelX) / tilesz;
		int yotile = (ry - mapModelY) / tilesz;
		
		// how many tiles to display on screen
		// have to do +2 here because int cast rounds down and div rounds down (?)
		int tilesw = (int) ((c.radius * 2) / tilesz) + 2;
		
		Intersection i = null;
		
		// for each tile on screen
		for (int x = 0; x < tilesw; x++) {
			for (int y = 0; y < tilesw; y++) {
				// is it a valid tile
				int xt = xotile + x;
				int yt = yotile + y;
				if (xt >= 0 && xt < mapArray.length && yt > 0 && yt < mapArray[0].length) {
					
					// is it none zero
					int b = mapArray[xt][yt] & 0xff;
					if (b != 0) {
						// get model x,y of tile
						int mtx = mapModelX + ((xotile + x) * tilesz);
						int mty = mapModelY + ((yotile + y) * tilesz);
						
						// should be image size rather than tile size
						Rect r = new Rect(mtx, mty, mtx + tilesz, mty + tilesz);
						Intersection i2 = CSIntersect.intersect(r, c, ctx, cty, 0.50f);
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
							// did not find intersection
							//annoTiles.add((xt << 16) + yt);
						}
					}
				}
			}
		}
		
		// XXX may need to reflect reflection...
		return i;
	}
	
	
}

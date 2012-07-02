package el.fg;

import java.awt.*;
import java.util.StringTokenizer;

import el.phys.Circle;
import el.Model;

/**
 * Object with a position, can paint and update and receive input if it has the focus
 */
// should probably merge this with MovingFgObject and give model object momentum
public abstract class FgObject extends Circle {
	
	/**
	 * Model of this object.
	 * Never null after object is added to model
	 */
	protected Model model;
	/**
	 * Object identifier
	 */
	protected int id;
	
	public FgObject() {
		//
	}
	
	/** update object from string */
	public final void read(String data) {
		readImpl(new StringTokenizer(data));
	}
	
	/** update object from string */
	protected void readImpl(StringTokenizer tokens) {
		int id = Integer.parseInt(tokens.nextToken());
		if (this.id != 0 && this.id != id) {
			throw new RuntimeException("received new id");
		}
		this.id = id;
    	x = Float.parseFloat(tokens.nextToken());
		y = Float.parseFloat(tokens.nextToken());
		radius = Float.parseFloat(tokens.nextToken());
    }
	
	/** write object to string */
	public final String write() {
		return writeImpl(new StringBuilder()).toString();
	}
    
	/** write object to string */
	protected StringBuilder writeImpl(StringBuilder sb) {
    	sb.append(id).append(" ");
    	sb.append(x).append(" ");
    	sb.append(y).append(" ");
    	sb.append(radius).append(" ");
    	return sb;
    }
    
    public final int getId() {
    	return id;
    }
    
	/**
	 * Current X position as closest integer
	 */
	public final int getX() {
		// int conversion will round down
		// as all model co-ordinates are positive, add 0.5 to avoid e.g. 4.9 -> 4
		return (int) (x + 0.5f);
	}
	
	/**
	 * Current Y position as closest integer
	 */
	public final int getY() {
		return (int) (y + 0.5f);
	}
	
	/**
	 * Width of object (for view clipping), default 0.
	 */
	public final int getRadius() {
		return (int) (radius + 0.5f);
	}
	
	/**
	 * Set the model of the object
	 */
	public final void setModel(Model model) {
		this.model = model;
	}
	
	/**
	 * Set the unique identifier of the object
	 */
	public final void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Paint. Do not leave any net transformation in subclass implementation.
	 */
	public abstract void paint(Graphics2D g);
	
    /** 
     * Update object state for time and delta time in seconds 
     */
    public void update (float t, float td) {
    	//
    }
    
    /**
     * User pressed up
     */
	public void up(float t, float td) {
		//
	}
	
	/**
     * User pressed down
     */
	public void down(float t, float td) {
		//
	}
	
	/**
     * User pressed left
     */
	public void left(float t, float td) {
		//
	}
	
	/**
     * User pressed right
     */
	public void right(float t, float td) {
		//
	}
	
	/**
     * User pressed fire 0-3.
     * return true if server update needed (e.g. for recoil)
     */
	public boolean fire(int n, float t, float td) {
		return false;
	}
	
	@Override
	public String toString() {
		return String.format("FgObject[%d]", id) + super.toString();
	}
	
}

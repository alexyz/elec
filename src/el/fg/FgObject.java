package el.fg;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.StringTokenizer;

import el.phys.Circle;
import el.Model;
import el.phys.Intersection;

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
	public void paint(Graphics2D g) {
		AffineTransform t = g.getTransform();
		paintAuto(g);
		g.setTransform(t);
	}
	
	/**
	 * Paint delegate. OK to leave a net transformation in subclass implementation.
	 */
	@Deprecated
	protected void paintAuto(Graphics2D g) {
		g.drawString(getClass().getSimpleName(), 0, 0);
	}
	
    /** 
     * Update object state for time and delta time in seconds 
     */
    public void update (float t, float td) {
    	//
    }
    
    public Intersection intersects(Circle c, float tx, float ty) {
    	return null;
    }

    /*
    public Intersection intersects(float rx1, float ry1, float rx2, float ry2, float rtx, float rty) {
    	return Intersect.intersect(mx - (width / 2), my - (height / 2), mx + (width / 2), my + (height / 2), rx1, ry1, rx2, ry2, rtx, rty);
    }
    */
    
    /**
     * Hit by this transient object
     * FIXME unused
     */
    public void hit(FgObject o) {
    	System.out.println("hit by " + o);
    }
    
    /**
     * User pressed up
     */
	public void up() { }
	
	/**
     * User pressed down
     */
	public void down() { }
	
	/**
     * User pressed left
     */
	public void left() { }
	
	/**
     * User pressed right
     */
	public void right() { }
	
	/**
     * User pressed fire 0-3
     */
	public void fire(int n) { }
	
	@Override
	public String toString() {
		return String.format("FgObject[%d]", id) + super.toString();
	}
	
}

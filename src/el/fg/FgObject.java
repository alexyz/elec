package el.fg;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.StringTokenizer;

import el.phys.Circle;
import el.phys.QuadMap;
import el.Model;
import el.phys.Intersection;

/**
 * Object with a position, can paint and update and receive input if it has the focus
 * TODO should probably merge this with MovingFgObject
 */
public abstract class FgObject implements QuadMap.XYR {
	
	/**
	 * Location of object
	 */
	protected final Circle c;
	/**
	 * Model of this object.
	 * Never null after object is added to model
	 */
	protected Model model;
	/**
	 * Object identifier
	 */
	protected int id;
	
	/**
	 * Create object with given location and radius
	 */
	public FgObject(Circle c) {
		this.c = c;
	}
	
	/** update object from string */
	public void read(StringTokenizer tokens) {
    	c.x = Float.parseFloat(tokens.nextToken());
		c.y = Float.parseFloat(tokens.nextToken());
		c.r = Float.parseFloat(tokens.nextToken());
    }
    
	/** write object to string */
    public StringBuilder write(StringBuilder sb) {
    	sb.append(c.x).append(" ");
    	sb.append(c.y).append(" ");
    	sb.append(c.r).append(" ");
    	return sb;
    }
    
    public final int getId() {
    	return id;
    }
    
    public final Circle getPosition() {
    	return c;
    }
	
	/**
	 * Current X position as closest integer
	 */
	public final int getX() {
		// int conversion will round down
		// as all model co-ordinates are positive, add 0.5 to avoid e.g. 4.9 -> 4
		return (int) (c.x + 0.5f);
	}
	
	/**
	 * Current Y position as closest integer
	 */
	public final int getY() {
		return (int) (c.y + 0.5f);
	}
	
	// FIXME
	public int getR() {
		return getRadius();
	}
	
	/**
	 * Width of object (for view clipping), default 0.
	 */
	public final int getRadius() {
		return (int) (c.r + 0.5f);
	}
	
	/**
	 * Set the model and identifier of the object
	 */
	public final void setModel(Model model, int id) {
		this.model = model;
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
		return String.format("FgObject[%s]", c);
	}
	
}

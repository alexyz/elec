package el.fg;
import java.awt.*;
import java.awt.geom.AffineTransform;

import el.phys.Circle;
import el.Model;
import el.phys.IntersectionFunction;
import el.phys.Intersection;


/**
 * Object with a position, can paint and update and receive input if it has the focus
 */
public abstract class FgObject {
	/**
	 * Model of this object.
	 * Never null after object is added to model
	 */
	protected Model model;
	/**
	 * Set to true for the object to be removed
	 */
	protected boolean remove;
	/**
	 * Location of object
	 */
	protected Circle c;
	
	public FgObject(Circle c) {
		this.c = c;
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
	
	/**
	 * Width of object (for view clipping), default 0.
	 */
	public final int getRadius() {
		return (int) (c.r + 0.5f);
	}
	
	/**
	 * Set the model
	 */
	public final void setModel(Model model) {
		this.model = model;
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
    
    /**
     * Returns true if object should be removed
     */
    public final boolean remove() {
    	return remove;
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
	
	public String write() {
		throw new RuntimeException();
	}
	
	@Override
	public String toString() {
		return String.format("FgObject[%s]", c);
	}
	
}

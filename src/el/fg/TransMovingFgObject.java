package el.fg;

import java.awt.Graphics2D;

import el.phys.Circle;

/**
 * Transient object (removed after specified time)
 */
public abstract class TransMovingFgObject extends MovingFgObject {
	
	/**
	 * Start and end time
	 */
	protected final float startt, endt;
	/**
	 * Set to true for the object to be removed
	 */
	protected boolean remove;
	
	public TransMovingFgObject(Circle c, float startt, float endt) {
		super(c);
		this.startt = startt;
		this.endt = endt;
	}
	
	/**
	 * Returns true if object should be removed
	 */
	public final boolean remove() {
		return remove;
	}
	
	@Override
	public void update(float t, float td) {
		if (t > endt) {
			remove = true;
			
		} else {
			super.update(t, td);
		}
	}
	
	@Override
	public void paint(Graphics2D g) {
		float t = model.getTime();
		float p = (t - startt) / (endt - startt);
		paint(g, p);
	}
	
	/**
	 * paint at given time parameter (0-1)
	 */
	public void paint(Graphics2D g, float p) {
		//
	}
	
	@Override
	public String toString() {
		return String.format("Trans[%f]", endt) + super.toString();
	}
	
	
}

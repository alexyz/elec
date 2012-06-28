package el.fg;

import java.awt.Graphics2D;
import java.util.StringTokenizer;

import el.phys.Circle;

/**
 * Transient object (removed after specified time)
 */
public abstract class TransObject extends MovingObject {
	
	/**
	 * Start and end time
	 */
	protected float startt, endt;
	
	/**
	 * Set to true for the object to be removed
	 */
	protected boolean remove;
	
	public TransObject(Circle c, float startt, float endt) {
		super(c);
		this.startt = startt;
		this.endt = endt;
	}
	
	@Override
	public void read(StringTokenizer tokens) {
		super.read(tokens);
		this.startt = Float.parseFloat(tokens.nextToken());
		this.endt = Float.parseFloat(tokens.nextToken());
	}
	
	@Override
	public StringBuilder write(StringBuilder sb) {
		super.write(sb);
		sb.append(startt).append(" ");
		sb.append(endt).append(" ");
		return sb;
	}
	
	/**
	 * Returns true if object should be removed
	 */
	public final boolean isRemove() {
		return remove;
	}
	
	@Override
	public void update(float floatTime, float floatTimeDelta) {
		if (floatTime > endt) {
			remove = true;
			
		} else {
			super.update(floatTime, floatTimeDelta);
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

package el.phys.cs;

import el.phys.*;

/**
 * Moving circle vs fixed square collision detection
 */
public class CSIntersect extends IntersectionFunction {
	
	@Override
	public Intersection intersect(Rect r, Circle c, float tx, float ty, float bounce) {
		return I3Test.intersectsdebug(r, c, tx, ty, bounce);
	}
	
}

package el.phys.cs;

import el.phys.*;

/**
 * Moving circle vs fixed square collision detection
 */
public class CSIntersect extends Intersect {
	
	/**
	 * Return true if moving circle intersects with fixed square.
	 * Optionally returns first intersection point (from l1 to l2).
	 */
	public static Intersection intersect_(Rect r, Circle c, float tx, float ty, float bf) {
		
		// subtract radius of circle from square to make it a point
		float rx0 = r.x0 - c.r;
		float rx1 = r.x1 + c.r;
		float ry0 = r.y0 - c.r;
		float ry1 = r.y1 + c.r;

		// consider translation line l1->l2 to be parameterised, e.g.
		// any point on line = l1 + p(l2-l1) where p is 0 to 1
		// now find values of p for the top, bottom, left and right of square (might not be 0-1)
		
		float px0 = (rx0 - c.x) / tx;
		//_x0 = c.x + tx * px0;
		//_y0 = c.y + ty * px0;

		float px1 = (rx1 - c.x) / tx;
		//_x1 = c.x + tx * px1;
		//_y1 = c.y + ty * px1;
		
		float py0 = (ry0 - c.y) / ty;
		//_x2 = c.x + tx * py0;
		//_y2 = c.y + ty * py0;
		
		float py1 = (ry1 - c.y) / ty;
		//_x3 = c.x + tx * py1;
		//_y3 = c.y + ty * py1;


		// if parameters for both x and y overlap, it intersects
		// there are usually two points of intersection (entry and exit), return the one closest to l1 only

		float pxh, pxl;
		if (px0 > px1) {
			pxh = px0;
			pxl = px1;
		} else {
			pxh = px1;
			pxl = px0;
		}
		
		float pyh, pyl;
		if (py0 > py1) {
			pyh = py0;
			pyl = py1;
		} else {
			pyh = py1;
			pyl = py0;
		}

		if (!(pxh >= pyl && pxl <= pyh)) {
			//System.out.println("does not overlap");
			return null;
		}

		// if i0..i2 and i4..i6 overlap
		// pick the second lowest of all four (either i0 or i4)
		float p, vx;
		if (pxl > pyl) {
			p = pxl;
			vx = -bf;
		} else {
			p = pyl;
			vx = bf;
		}

		if (p < 0f || p > 1f) {
			// line would intersect but it is not long enough
			//System.out.println("does not reach");
			return null;
		}

		float idx = tx * p;
		float idy = ty * p;
		// TODO some way to detect corner miss
		// but it requires the entire path falls outside the corner radius
		// not just the intersection point
		
		//System.out.println("intersects at " + p);
		Intersection in = new Intersection();
		in.itx = idx;
		in.ity = idy;
		// can only bounce in one axis...
		in.vx = vx;
		in.vy = -vx;
		
		// p after reflection, if required
		if (bf > 0) {
			float pp = (1f - p);
			in.rtx = in.itx + (in.vx * tx * pp);
			in.rty = in.ity + (in.vy * ty * pp);
		}

		return in;
	}
	
	@Override
	public Intersection intersect(Rect r, Circle c, float tx, float ty, float bounce) {
		return intersect_(r, c, tx, ty, bounce);
	}
	
}

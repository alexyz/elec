package el.phys.cs;

import el.phys.*;

/**
 * Moving circle vs fixed square collision detection
 */
public class CSIntersect {
	
	/**
	 * rotate point around centre of rectangle
	 */
	public static Point rotate(Rect r, float theta, Point p) {
		float rxo = r.x0 + (r.x1 - r.x0) / 2;
		float ryo = r.y0 + (r.y1 - r.y0) / 2;
		float x = p.x - rxo;
		float y = p.y - ryo;
		float scale = FloatMath.hypot(x, y);
		float a = FloatMath.atan2(x, y) + theta;
		float x2 = FloatMath.sin(a) * scale + rxo;
		float y2 = FloatMath.cos(a) * scale + ryo;
		return new Point(x2, y2);
	}
	
	// circle : point, rect : point
	
	/**
	 * find intersection with rotated rectangle
	 */
	public static Intersection superintersect(Rect r, float theta, Circle c, float tx, float ty, float bounceFactor) {
		Point p = new Point(c.x, c.y);
		Point p2 = rotate(r, theta, p);
		//System.out.println("c " + p + " rotate " + theta + " is " + p2);
		Circle c2 = new Circle(p2.x, p2.y, c.r);
		
		Point pt = new Point(c.x + tx, c.y + ty);
		Point pt2 = rotate(r, theta, pt);
		//System.out.println("ct " + pt + " rotate " + theta + " is " + pt2);
		
		float tx2 = pt2.x - p2.x;
		float ty2 = pt2.y - p2.y;
		//System.out.println("t " + new Point(tx, ty) + " rotate " + theta + " is " + new Point(tx2, ty2));
		Intersection i2 = intersect(r, c2, tx2, ty2, bounceFactor);
		System.out.println("superintersection: " + i2);
		
		if (i2 != null) {
			// this doesn't work
			
			Point ip = rotate(r, FloatMath.twopi - theta, new Point(c2.x + i2.itx, c2.y + i2.ity));
			i2.itx = ip.x - c.x;
			i2.ity = ip.y - c.y;
		}
		
		return i2;
	}
		
	
	/**
	 * Return true if moving circle intersects with fixed orthogonal rectangle.
	 * Optionally returns first intersection point (from l1 to l2).
	 */
	public static Intersection intersect(Rect r, Circle c, float tx, float ty, float bounceFactor) {
		
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
			vx = -bounceFactor;
		} else {
			p = pyl;
			vx = bounceFactor;
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
		if (bounceFactor > 0) {
			float pp = (1f - p);
			in.rtx = in.itx + (in.vx * tx * pp);
			in.rty = in.ity + (in.vy * ty * pp);
		}

		return in;
	}
	
}

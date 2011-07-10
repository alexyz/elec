package el.phys.ss;

import el.phys.Intersection;

public class Impl {
	/**
	 * S is fixed square, T is moving square
	 */
	public static Intersection squareIntersect(
			final float sx1, final float sy1, final float sx2, final float sy2, 
			final float tx1, final float ty1, final float tx2, final float ty2, 
			final float dx, final float dy) {
		
		// for some reason it thinks a zero delta is a collision
		if (dx == 0f && dy == 0f)
			return null;
		
		// find closest x/y edge
		final float tx, sx;
		if (dx <= 0) {
			tx = tx1;
			sx = sx2;
		} else {
			tx = tx2;
			sx = sx1;
		}
		
		final float ty, sy;
		if (dy <= 0) {
			ty = ty1;
			sy = sy2;
		} else {
			ty = ty2;
			sy = sy1;
		}
		
		// find param of x/y collision
		// avoid div by 0 (not a runtime exception with floats)
		final float p1 = dx != 0f ? (sx - tx) / dx : 0f;
		final float p1y = ty + (dy * p1);
		final float p2 = dy != 0f ? (sy - ty) / dy : 0f;
		final float p2x = tx + (dx * p2);
		
		final float th = ty2 - ty1;
		final float tw = tx2 - tx1;
		
		final float i, vx, vy;
		if (p1 == p2 && p1 >= 0f && p1 <= 1f) {
			i = p1;
			vx = -1f;
			vy = -1f;
			
		} else if (p1 > p2 && p1 >= 0f && p1 <= 1f && p1y >= sy1 - th && p1y <= sy2 + th) {
			i = p1;
			vx = -1f;
			vy = 1f;
			
		} else if (p2 > p1 && p2 >= 0f && p2 <= 1f && p2x >= sx1 - tw && p2x <= sx2 + tw) {
			i = p2;
			vx = 1f;
			vy = -1f;
			
		} else {
			return null;
		}
		
		Intersection r = new Intersection();
		// new location without reflection
		r.itx = dx * i;
		r.ity = dy * i;
		// new location with reflection
		r.rtx = r.itx + vx * dx * (1f - i);
		r.rty = r.ity + vy * dy * (1f - i);
		// velocity change with reflection
		r.vx = vx;
		r.vy = vy;
		
		//System.out.printf("s=%f,%f-%f,%f t=%f,%f-%f,%f d=%f,%f\n", sx1, sy1, sx2, sy2, tx1, ty1, tx2, ty2, dx, dy);
		
		return r;
	}
}
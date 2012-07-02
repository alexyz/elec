package el.phys.cs;

import el.phys.Circle;
import el.phys.Trans;


public class CS2 {
	
	public I intersect(R r, Circle c, Trans t, float bf) {
		
		float rx;
		if (t.x > 0) {
			rx = r.x;
		} else {
			rx = r.x + r.w;
		}
		
		float ry;
		if (t.y > 0) {
			ry = r.y;
		} else {
			ry = r.y + r.h;
		}
		
		
			
		
		
		return null;
	}
	
}

class R {
	float x, y, w, h;
}

class I {
	float x, y;
	float vx, vy;
}

	
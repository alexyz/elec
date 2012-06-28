package el.fg;

import el.phys.QuadMap;

public class BulletQuadMapKey implements QuadMap.Key<BulletObject> {

	@Override
	public int getX(BulletObject obj) {
		return obj.getX();
	}

	@Override
	public int getY(BulletObject obj) {
		return obj.getY();
	}

	@Override
	public int getR(BulletObject obj) {
		return obj.getProximity();
	}
	
}
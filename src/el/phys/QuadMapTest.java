package el.phys;

import java.util.ArrayList;
import java.util.Random;

class QuadMapTest {

	public static void main(String[] args) {
		Random r = new Random();
		
		QuadMap.Key<QuadMapTest> key = new QuadMap.Key<QuadMapTest>() {
			@Override
			public int getX(QuadMapTest obj) {
				return obj.x;
			}
			@Override
			public int getY(QuadMapTest obj) {
				return obj.y;
			}
			@Override
			public int getR(QuadMapTest obj) {
				return obj.r;
			}
		};
		
		QuadMap<QuadMapTest> q = new QuadMap<QuadMapTest>(key);
		ArrayList<QuadMapTest> l = new ArrayList<QuadMapTest>();
		for (int n = 0; n < 10000; n++) {
			final int x = r.nextInt(10000) + 495000;
			final int y = r.nextInt(10000) + 495000;
			QuadMapTest obj = new QuadMapTest(x, y, 10);
			q.add(obj);
			l.add(obj);
		}
		System.out.println("quadmap is " + q);
		//ArrayList<MyXY> l = q.slowget(495000, 495000, 1000);
		//System.out.println("slow query returns " + l);
		ArrayList<QuadMapTest> l2 = q.get(495000, 495000, 1000);
		System.out.println("fast query returns " + l2);
		for (int n = 0; n < 10; n++) {
			System.out.println("updating");
			for (QuadMapTest obj : l) {
				int oldx = obj.x, oldy = obj.y;
				obj.x = r.nextInt(10000) + 495000;
				obj.y = r.nextInt(10000) + 495000;
				q.update(obj, oldx, oldy);
			}
			System.out.println("quadmap is " + q);
		}
	}
	
	public int x;
	public int y;
	public int r;
	
	public QuadMapTest(int x, int y, int r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}
	
	@Override
	public String toString() {
		return String.format("(%d,%d)", x, y);
	}
}

package el.fg;

import el.ClientFrame;

public class ShipType {
	
	public static final ShipType[] types = new ShipType[1];
	
	static {
		ShipType type1 = new ShipType();
		type1.name = "Eagle";
		type1.img = ClientFrame.SHIP1_IMAGE;
		type1.maxv = 1000f;
		type1.xres = 0.9f;
		type1.yres = 0.9f;
		type1.rotres = 0.25f;
		type1.guns = new Gun[3];
		type1.guns[0] = new Gun(Gun.Type.gun2, 100f, 0.2f, 150f, new Mount(10, -15, 0), new Mount(-10, -15, 0));
		type1.guns[1] = new Gun(Gun.Type.bomb2, 100f, 0.5f, 150f, new Mount(0, -15, 0));
		type1.guns[2] = new Gun(Gun.Type.bomb4, 100f, 1f, 200f, new Mount(0, -8, 0));
		types[0] = type1;
	}
	
	String name;
	String img;
	float maxv;
	float xres;
	float yres;
	float rotres;
	float maxenergy;
	Gun[] guns;
	
}

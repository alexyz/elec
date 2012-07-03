package el.inf;

import java.util.*;

import el.ClientFrame;


public class ZoneInfo {
	public Map<String,BulletType2> bullets = new TreeMap<String,BulletType2>();
	public Map<String,ShipType2> ships = new TreeMap<String,ShipType2>();
	public Map<String,GunType2> guns = new TreeMap<String,GunType2>();
	public Map<String,MountType2> mounts = new TreeMap<String,MountType2>();
	
	public ZoneInfo() {
		
		bullets.put("bullet2", new BulletType2(2, 150f, 100, 5, 5));
		bullets.put("bomb2", new BulletType2(2, 150f, 100, 10, 25));
		
		mounts.put("mount1left", new MountType2(-10, -15, 0));
		mounts.put("mount1right", new MountType2(10, -15, 0));
		
		MountType2[] m = new MountType2[] { mounts.get("mount1left"), mounts.get("mount1right") };
		
		guns.put("gun1", new GunType2(bullets.get("bullet2"), m, 0.5f));
		
		ShipType2 type1 = new ShipType2();
		type1.name = "Eagle";
		type1.img = ClientFrame.SHIP1_IMAGE;
		type1.maxv = 1000f;
		type1.xres = 0.9f;
		type1.yres = 0.9f;
		type1.rotres = 0.25f;
		type1.gun = guns.get("gun1");
		type1.bomb = guns.get("bomb1");
	}
}

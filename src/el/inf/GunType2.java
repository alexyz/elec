
package el.inf;

public class GunType2 {
	
	public float period;
	public MountType2[] mounts;
	public BulletType2 bulletType;
	
	public GunType2(BulletType2 bulletType, MountType2[] mountTypes, float period) {
		this.period = period;
		this.bulletType = bulletType;
		this.mounts = mountTypes;
	}
}

package el.serv;

/**
 * Commands sent from server to client
 */
public interface ClientCommands {

	/** tell client new server time */
	public void setTime(long time);
	/** tell client with id to enter */
	public void playerEntered(int id, int freq, float x, float y, boolean msg);
	/** tell client to spectate */
	public void playerSpectated(int id);
	/** tell client its identifier */
	public void setId(int id, String name);
	/** tell client to update the given object */
	public void updateFgObject(int id, String data);
	/** tell client to discard any existing map and use given map data */
	public void setMapData(String data);
	/** tell client to update map tile */
	public void setMapTile(int x, int y, int action);
	/** tell client to add bullet */
	public void addBullet(String data);
	/** send chat to client */
	public void addMsg(int id, String msg);
	/** tell client someone has connected - should send update in response */
	public void playerConnected(int id, String name);
	/** tell client someone has exited */
	public void playerExited(int id);
	/** tell client the bullet has exploded */
	public void bulletExploded(int transid);
	/** tell client someone was killed */
	public void playerKilled(int id, int killerId, float x, float y);
	/** ping received */
	public void pong();
}

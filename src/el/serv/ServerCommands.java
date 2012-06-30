package el.serv;

/**
 * Asynchronous commands sent from client to server.
 */
public interface ServerCommands {

	/** ask server if we can enter */
	public void enterReq();
	/** tell server we are going to spectate */
	public void spec();
	/** update details of players ship */
	public void update(String data);
	/** update map tile request */
	public void setMapTile(int x, int y, int action);
	/** fire transient */
	public void addBullet(String data);
	/** send text message to server */
	public void addMsg(String msg);
	/** send client name to server (should send ID back) */
	public void setName(String name);
	/** send hit to server */
	public void playerHit(int transid);
	/** current players ship was killed */
	public void playerKilled(int killedId, float x, float y);
	/** request ping */
	public void ping();
	
}

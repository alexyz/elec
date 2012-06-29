package el.serv;

/**
 * Commands sent from client to server
 */
public interface ServerCommands {

	/** ask server if we can enter (void) */
	public static final String ENTERREQ = "enter-req";
	/** tell server we are going to spectate (void) */
	public static final String SPEC = "spec";
	/** update details of players ship */
	public static final String UPDATE = "update";
	/** update map tile request */
	public static final String MAPTILEREQ = "map-tile-req";
	/** fire transient */
	public static final String FIREREQ = "fire-req";
	/** send text message to server (String) */
	public static final String TALKREQ = "talk-req";
	/** send client name to server (String) (should send ID back) */
	public static final String NAME = "name";
	/** send hit to server (int transid) */
	public static final String HIT = "hit";
	/** send killed to server (int killerId, float x, float y) */
	public static final String KILLED = "killed";
	
}

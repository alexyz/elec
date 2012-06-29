package el.serv;

/**
 * Commands sent from server to client
 */
public interface ClientCommands {

	/** tell client new server time (long) */
	public static final String TIME = "time";
	/** tell client with id to enter (int id, int freq, float x, float y, boolean msg) */
	public static final String ENTER = "enter";
	/** tell client to spectate (int) */
	public static final String SPEC = "spectate";
	/** tell client its identifier (int) */
	public static final String ID = "id";
	/** tell client to update the given object (int id, FGObject) */
	public static final String UPDATEOBJ = "update-obj";
	/** tell client to discard any existing map and use given map data (MapBgObject) */
	public static final String MAPDATA = "map-data";
	/** tell client to update map tile */
	public static final String MAPTILE = "map-tile";
	/** tell client to add bullet */
	public static final String FIRE = "fire";
	/** send chat to client (int id, String msg) */
	public static final String TALK = "talk";
	/** tell client someone has connected (int id, String name) - should send update in response */
	public static final String CONNECTED = "connected";
	/** tell client someone has exited (int id) */
	public static final String EXIT = "exited";
	/** tell client the bullet has exploded (int transid) */
	public static final String EXPLODE = "explode";
	/** tell client someone was killed (int id, int killerId, float x, float y) */
	public static final String KILL = "kill";
}

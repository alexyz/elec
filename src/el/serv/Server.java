package el.serv;

import el.fg.Ship;

/**
 * Interface for sending commands from client to server
 */
public interface Server {
	
	public void enterReq();
	
	public void specReq();
	
	/**
	 * send details of the focused object to server
	 */
	public void update(Ship ship);
	
	public long getTime();
}

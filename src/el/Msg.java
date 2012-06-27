package el;

public class Msg {
	
	public final int type;
	public final String msg;
	public final String sender;

	public Msg(int type, String sender, String msg) {
		this.type = type;
		this.sender = sender;
		this.msg = msg;
	}
}

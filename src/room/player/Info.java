package room.player;

public class Info {
	Player player;
	String msgString;
	public Info() {}
	public Info(Player player, String str) {
		this.player = player;
		this.msgString = str;
	}
	public Player getPlayer() {
		return player;
	}
	public String getMsgString() {
		return msgString;
	}
}

package room.format;

public class GobangMapFormat {
    private String name;
    private int avatarID;
    private int playerID;
    private int time;
    private int byoTime;
    private int byoCount;
    public int getAvatarID() {
        return avatarID;
    }
    public int getByoCount() {
        return byoCount;
    }
    public int getByoTime() {
        return byoTime;
    }
    public String getName() {
        return name;
    }
    public int getPlayerID() {
        return playerID;
    }
    public int getTime() {
        return time;
    }
    public void setAvatarID(int avatarID) {
        this.avatarID = avatarID;
    }
    public void setByoCount(int byoCount) {
        this.byoCount = byoCount;
    }
    public void setByoTime(int byoTime) {
        this.byoTime = byoTime;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }
    public void setTime(int time) {
        this.time = time;
    }
}

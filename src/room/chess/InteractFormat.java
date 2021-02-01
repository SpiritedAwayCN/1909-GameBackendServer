package room.chess;

public class InteractFormat {
    private int type;
    private Object status;
    private String msg;
    private int playerID;
    public String getMsg() {
        return msg;
    }
    public int getPlayerID() {
        return playerID;
    }
    public Object getStatus() {
        return status;
    }
    public int getType() {
        return type;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }
    public void setStatus(Object status) {
        this.status = status;
    }
    public void setType(int type) {
        this.type = type;
    }
}

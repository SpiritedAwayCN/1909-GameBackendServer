package room.format;

public class GobangStatusFormat {
    int code;
    int number;
    int time;
    int byoTime;
    int byoCount;
    int turnID;
    public int getByoCount() {
        return byoCount;
    }
    public int getByoTime() {
        return byoTime;
    }
    public int getCode() {
        return code;
    }
    public int getNumber() {
        return number;
    }
    public int getTime() {
        return time;
    }
    public int getTurnID() {
        return turnID;
    }
    public void setByoCount(int byoCount) {
        this.byoCount = byoCount;
    }
    public void setByoTime(int byoTime) {
        this.byoTime = byoTime;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public void setTime(int time) {
        this.time = time;
    }
    public void setTurnID(int turnID) {
        this.turnID = turnID;
    }
}

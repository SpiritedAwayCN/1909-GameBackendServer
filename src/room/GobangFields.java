package room;

public class GobangFields {
    protected int time = 20000;
    protected int byoTime = 10000;
    protected int byoCount = 3;

    GobangFields(){}
    GobangFields(int time, int byoTime, int byoCount){
        this.time = time;
        this.byoTime = byoTime;
        this.byoCount = byoCount;
    }

    public int getByoCount() {
        return byoCount;
    }
    public int getByoTime() {
        return byoTime;
    }
    public int getTime() {
        return time;
    }
    public void setByoCount(int byoCount) {
        this.byoCount = byoCount;
    }
    public void setByoTime(int byoTime) {
        this.byoTime = byoTime;
    }
    public void setTime(int time) {
        this.time = time;
    }
    public int decreaseByoCount(){
        this.byoCount -= 1;
        return this.byoCount;
    }

    public int getTimeServer(){
        if(time == -1) return getByoTimeServer();
        lastMillion = System.currentTimeMillis();
        return time + 3000;
    }
    private int getByoTimeServer() {
        lastMillion = System.currentTimeMillis();
        return byoTime + 3000;
    }

    private long lastMillion = -1;
    public void resetTimer(){
        lastMillion = -1;
    }
    public void resetTimer(long ms){
        time -= (int)(ms - lastMillion);
        if(time < 0) time = -1;
        lastMillion = -1;
    }

    public int getRemainTime(long ms){
        if(lastMillion == -1) return time;
        int ans = time - (int)(ms - lastMillion);
        if(ans < 0) ans = -1;
        return ans;
    }
    public int getRemainByoTime(long ms){
        if(lastMillion == -1 || time > -1) return byoTime;
        int ans = byoTime - (int)(ms - lastMillion);
        if(ans < 0) ans = 0;
        return ans;
    }
}

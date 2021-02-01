package room;

import java.util.*;

import com.alibaba.fastjson.JSON;

import player.*;
import room.chess.*;

public class GobangRoom extends GameRoom {
	public static final int roomTypeID = 1;
	private static final int BOARDSIZE = 15;
	int board[][] = new int[BOARDSIZE][BOARDSIZE];
	int currentId;
	private Timer timer;

	private ArrayList<ChessField> fields = new ArrayList<>();
	
	public GobangRoom() {}
	public GobangRoom(int id) {
		super(id);
		maxPlayer = 2;
	}

	@Override
	public boolean joinPlayer(Player p) {
		if(status != RoomStatus.WATING) return false;
		if(maxPlayer <= playerCounter) return false;
		
		p.setRoom(this);
		p.setIsReady(false);
		p.setId(playerCounter);
		
		synchronized(this){
			this.players.add(p);
			this.playerCounter += 1;
		}

		p.setRecvQueue(this.infoQueue);
		p.getHall().leavePlayer(p);		
		return true;
	}

	@Override
	public void gameStart() {
		System.gc();
		super.gameStart();
		fields.clear();

		/* If next round, swap the black&white, not random */
		Player tempPlayer = players.get(0);
		players.set(0, players.get(1));
		players.set(1, tempPlayer);

		for (int i = 0; i < players.size(); i++) {
			players.get(i).setId(i + 1);
			fields.add(new ChessField());
		}

		for(int i = 0; i < BOARDSIZE; i++)
			for(int j = 0; j < BOARDSIZE; j++)
				board[i][j] = 0;
		
		currentId = 1;
		Map<String, Object> map = getGameStatusMap();
		Map<String, Object> map2 = new HashMap<>();
		map2.put("type", 4);
		map2.put("player", map);
		for(Player p: players){
			map.put("yourID", p.getId());
			p.sendMsg(JSON.toJSONString(map2));
		}

		timer = new Timer();
		timer.schedule(new PlayerTimerTask(players.get(0)), fields.get(0).getTimeServer());
	}

	@Override
	public void runRunning() {
		outer: while(true) {
			Info info = null;
			try {
				info = infoQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			if(info.getMsgString().equals("timeout")){
				if(info.getPlayer().getId() != currentId) continue outer;
				timer.cancel();
				// System.out.println("Time out");
				Player p = info.getPlayer();
				ChessField field = fields.get(currentId - 1);
				InteractFormat interactFormat = new InteractFormat();
				interactFormat.setType(1);
				interactFormat.setPlayerID(currentId);
				if(field.getTime() >= 0){
					field.setTime(-1);
					interactFormat.setStatus(buildInterruptStatus(field.getByoTime(), field.getByoCount() , currentId));
					timer = new Timer();
					timer.schedule(new PlayerTimerTask(p), field.getTimeServer());
				}else if(field.decreaseByoCount() <= 0){
					interactFormat.setStatus(buildInterruptStatus(0, 0, -(currentId ^ 3)));
					String msg = JSON.toJSONString(interactFormat);
					for(Player player: players){
						player.sendMsg(msg);
					}
					break outer;
				}else{
					interactFormat.setStatus(buildInterruptStatus(field.getByoTime(), field.getByoCount() , currentId));
					timer = new Timer();
					timer.schedule(new PlayerTimerTask(p), field.getTimeServer());
				}
				String msg = JSON.toJSONString(interactFormat);
				for(Player player: players){
					player.sendMsg(msg);
				}
				continue outer;
			}
			
			if (info.getMsgString().equals("L!E@A#V$E%")) {
				InteractFormat interactFormat = new InteractFormat();
				interactFormat.setType(1);
				interactFormat.setPlayerID(info.getPlayer().getId());
				interactFormat.setStatus(buildInterruptStatus(0, 0, -(info.getPlayer().getId() ^ 3)));
				for(Player player: players){
					player.sendMsg(JSON.toJSONString(interactFormat));
				}
				info.getPlayer().onDisconnect();
				break outer;
			}

			try {
				Map<String, Object> map = JSON.parseObject(info.getMsgString());
				InteractFormat interactFormat = new InteractFormat();
				Player p = info.getPlayer();

				int type = (int)map.get("type");
				interactFormat.setType(type);

				switch (type) {
					case 0:
						interactFormat.setStatus(getGameStatusMap());
						p.sendMsg(JSON.toJSONString(interactFormat));
						continue outer;
					case 5: //chat
						interactFormat.setMsg((String)map.get("msg"));
						interactFormat.setPlayerID(p.getId());
						break;
					case 1: //chess
						interactFormat.setPlayerID(p.getId());
						{
							int x, y;
							StatusFormat statusFormat = new StatusFormat();
							ChessField field = fields.get(p.getId() - 1);
							long ms = System.currentTimeMillis();
							
							statusFormat.setTime(field.getRemainTime(ms));
							statusFormat.setByoCount(field.getByoCount());
							statusFormat.setByoTime(field.getByoTime());

							x = (int)map.get("number");
							statusFormat.setNumber(x);
							y = x % BOARDSIZE;
							x = x / BOARDSIZE;
							if(!addPiece(x, y, p.getId())){
								statusFormat.setCode(-1);
								statusFormat.setTurnID(currentId);
								interactFormat.setStatus(statusFormat);
								p.sendMsg(JSON.toJSONString(interactFormat));
								continue outer;
							}
							timer.cancel();
							statusFormat.setCode(0);
							if(judgeWinner(x, y)){
								statusFormat.setTurnID(-currentId);
								interactFormat.setStatus(statusFormat);
								String msg = JSON.toJSONString(interactFormat);
								for (Player player : players) {
									player.sendMsg(msg);
								}
								break outer;
							}
							field.resetTimer(ms);

							currentId ^= 3; // switch between 1 and 2
							timer = new Timer();
							timer.schedule(new PlayerTimerTask(players.get(currentId - 1)),
									fields.get(currentId - 1).getTimeServer());
							statusFormat.setTurnID(currentId);
							interactFormat.setStatus(statusFormat);
						}
						break;
					case 4: //escape
						timer.cancel();

						interactFormat.setPlayerID(p.getId());
						interactFormat.setStatus(buildInterruptStatus(0, 0, -(p.getId() ^ 3)));
						leavePlayer(p);
						p.getHall().joinPlayer(p, getRoomTypeID());
						break;
					default:
						continue outer;
				}
				String msg = JSON.toJSONString(interactFormat);
				for (Player player : players) {
					player.sendMsg(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
	}

	private StatusFormat buildInterruptStatus(int bt, int bc, int turnID){
		StatusFormat format = new StatusFormat();
		format.setCode(-2);
		format.setNumber(-1);
		format.setTime(-1);
		format.setByoTime(bt);
		format.setByoCount(bc);
		format.setTurnID(turnID);

		return format;
	}
	
	private static final int dx[]= {1, 1, 0, -1};
	private static final int dy[]= {0, 1, 1, 1};
	private boolean addPiece(int x, int y, int id){
		if(id != currentId) return false;
		if(x < 0 || x >= BOARDSIZE || y < 0 || y > BOARDSIZE || board[x][y] != 0) return false;
		board[x][y] = id;
		return true;
	}

	private boolean judgeWinner(int x, int y) {
		if(board[x][y] == 0) return false;
		for(int dir = 0; dir < 4; dir++) {
			int counter = -1;
			for(int xx = x, yy = y; true; xx += dx[dir], yy += dy[dir]) {
				if(xx < 0 || yy < 0 || xx >= BOARDSIZE || yy >= BOARDSIZE) break;
				if(board[xx][yy] == board[x][y]) {
					counter += 1;
				}else {
					break;
				}
			}
			
			for(int xx = x, yy = y; true; xx -= dx[dir], yy -= dy[dir]) {
				if(xx < 0 || yy < 0 || xx >= BOARDSIZE || yy >= BOARDSIZE) break;
				if(board[xx][yy] == board[x][y]) {
					counter += 1;
				}else {
					break;
				}
			}
			
			if(counter >= 5) return true;
		}
		return false;
	}

	@Override
	public boolean leavePlayer(Player p) {
		synchronized(this){
			players.remove(p);
			playerCounter -= 1;
			if(status == RoomStatus.WATING) {
				leavePlayerWhileWaiting(p);
			}
		}
		p.setRoom(null);
		p.setId(-1);
		return true;
	}
	
	public int[] board2Int() {
		int[] boardEncoded = new int[BOARDSIZE];
		for(int i = 0; i < BOARDSIZE; i++){
			int line = 0;
			for(int j = 0; j < BOARDSIZE; j++){
				line <<= 2;
				line |= board[i][j];
			}
			boardEncoded[i] = line;
		}
		return boardEncoded;
	}

	public Map<String, Object> getGameStatusMap(){
		Map<String, Object> map = new HashMap<>();
		ArrayList<MapFormat> playerFormats = new ArrayList<>();
		for(int i = 0; i < players.size(); i++){
			MapFormat format = new MapFormat();
			Player player = players.get(i);
			ChessField field = fields.get(i);

			long ms = System.currentTimeMillis();
			format.setAvatarID(player.getAvatarID());
			format.setByoCount(field.getByoCount());
			format.setByoTime(field.getRemainByoTime(ms));
			format.setName(player.getName());
			format.setPlayerID(player.getId());
			format.setTime(field.getRemainTime(ms));
			playerFormats.add(format);
		}

		map.put("playerMap", playerFormats);
		map.put("board", board2Int());
		map.put("turnID", currentId);
		return map;
	}

	@Override
	public int getRoomTypeID() {
		return roomTypeID;
	}

}

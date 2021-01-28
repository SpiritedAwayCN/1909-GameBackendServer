package room;

import player.Info;
import player.Player;

public class GobangRoom extends GameRoom {
	Player winner;
	private static final int BOARDSIZE = 15;
	int board[][] = new int[BOARDSIZE][BOARDSIZE];
	int currentId = -1;
	int firstId = -1;
	
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
		
		this.players.add(p);
		this.playerCounter += 1;
		
		return p.openStream();
	}

	@Override
	public void gameStart() {
		System.gc();
		super.gameStart();
		for(int i = 0; i < BOARDSIZE; i++)
			for(int j = 0; j < BOARDSIZE; j++)
				board[i][j] = 0;
		currentId = Math.random() < 0.5 ? 0 : 1;
		firstId = currentId;
		for(Player p: players) {
			p.sendMsg(board2String());
			if(p.getId() == firstId) {
				p.sendMsg("Your id: " + p.getId() + ", color: BLACK");
				p.sendMsg("Your turn now.");
			}else
				p.sendMsg("Your id: " + p.getId() + ", color: WHITE");
		}
		
	}

	@Override
	public void runRunning() {
		while(true) {
			Info info = null;
			try {
				info = infoQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			if(info.getMsgString().equals("q")) {
				info.getPlayer().sendMsg(board2String());
				continue;
			}else if (info.getMsgString().equals("L!E@A#V$E%")) {
				info.getPlayer().onDisconnect();
				break; //TODO
			}
			
			if(info.getPlayer().getId() != currentId) {
				info.getPlayer().sendMsg("Not your turn");
				continue;
			}
			String msg = info.getMsgString();
			String[] coordStrings = msg.split(" ");
			int x = -1, y = -1;
			try {
				x = Integer.parseInt(coordStrings[0]);
				y = Integer.parseInt(coordStrings[1]);
			} catch (Exception e) {
				info.getPlayer().sendMsg("Invalid!");
				continue;
			}
			if(coordStrings.length != 2 || x < 0 || x >= BOARDSIZE 
					|| y < 0 || y > BOARDSIZE || board[x][y] != 0) {
				info.getPlayer().sendMsg("Invalid!");
				continue;
			}
			board[x][y] = currentId == firstId ? 1 : 2;
			Player nextPlayer = null;
			for (Player p : players) {
				p.sendMsg((currentId == firstId ? "# " : "* ") + x + " " + y);
				if(p.getId() != currentId)
					nextPlayer = p;
			}
			
			if(judgeWinner(x, y)) {
				winner = info.getPlayer();
				break;
			}
			
			currentId = nextPlayer.getId();
			nextPlayer.sendMsg("Your turn now.");
		}
	}
	
	private static final int dx[]= {1, 1, 0, -1};
	private static final int dy[]= {0, 1, 1, 1};
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
	public void gameEnd() {
		for(Player player: players) {
			player.sendMsg("--END-- You " + (player == winner ? "win" : "lose"));
		}
		status = RoomStatus.WATING;
		
	}

	@Override
	public boolean leavePlayer(Player p) {
		// TODO 
		players.remove(p);
		playerCounter -= 1;
		if(status == RoomStatus.WATING) {
			leavePlayerWhileWaiting(p);
		}else {
			for(Player player: players) {
				player.sendMsg("Player " + p.getId() + " has left the room.");
			}
			winner = players.size() > 0 ? players.get(0) : null;
			// runEndding();
		}
		p.setRoom(null);
		p.setId(-1);
		return true;
	}
	

	
	public String board2String() {
		StringBuilder sb = new StringBuilder("$");
		for(int i = 0; i < BOARDSIZE; i++)
			for(int j = 0; j < BOARDSIZE; j++)
				sb.append(board[i][j]);
		return sb.toString();
	}

}

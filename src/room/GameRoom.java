package room;

import java.util.*;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSON;

import player.*;

public abstract class GameRoom implements Runnable {
	public enum RoomStatus {
		WATING, RUNNING
	}

	int roomID;
	LinkedList<Player> players = new LinkedList<Player>();
	private Map<String, Object> tempMap = new HashMap<>();
	public final BlockingQueue<Info> infoQueue = new ArrayBlockingQueue<Info>(20);

	protected class PlayerTimerTask extends TimerTask {
		Player player;
		PlayerTimerTask(Player player) {
			this.player = player;
		}
		@Override
		public void run() {
			while(true){
				try {
					infoQueue.put(new Info(player, "timeout"));
					break;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	RoomStatus status;
	int playerCounter, readyCounter;
	int maxPlayer;
	
	public GameRoom() {}
	public GameRoom(int id) {
		this.roomID = id;
		initRoom();
	}
	
	public void initRoom() {
		this.infoQueue.clear();
		this.status = RoomStatus.WATING;
		this.playerCounter = 0;
		this.readyCounter = 0;
	}

	public abstract int getRoomTypeID();
	
	public abstract boolean joinPlayer(Player p);
	public abstract boolean leavePlayer(Player p);
	
	protected void leavePlayerWhileWaiting(Player p) {
		if(p.getIsReady())
			readyCounter -= 1;
	}
	
	public void runWaiting() {
		outer: while(true) {
			Info info = null;
			try {
				info = infoQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			
			if (info.getMsgString().equals("join")) {
				Player p = info.getPlayer();
				if(p.getRoom() != null) {
					// p.sendMsg("Already Entered");
					continue;
				}else if(!joinPlayer(p)){
					Map<String, Object> map = new HashMap<>();
					map.put("code", -1);
					map.put("msg", "Fail to join");
					map.put("roomJson", getRoomInfoMap());
					continue;
				}
				Map<String, Object> map = new HashMap<>();
				map.put("code", 0);
				map.put("msg", "");
				map.put("roomJson", getPlayersStatus());
				info.getPlayer().sendMsg(JSON.toJSONString(map));
				
				tempMap.clear();
				tempMap.put("type", 5); // join
				tempMap.put("player", info.getPlayer().getBasicInfoMap());
				String jsonMsg = JSON.toJSONString(tempMap);
				for(Player player: players) {
					player.sendMsg(jsonMsg);
				}
				continue;
			}
			
			if (info.getMsgString().equals("L!E@A#V$E%")) {
				info.getPlayer().onDisconnect();
				continue;
			}

			try {
				Map<String, Object> map = JSON.parseObject(info.getMsgString());
				int type = (int)map.get("type");

				this.tempMap.clear();

				if(type == 0){
					tempMap.put("type", 0);
					tempMap.put("player", getPlayersStatus());
					info.getPlayer().sendMsg(JSON.toJSONString(tempMap));
					continue;
				}
				Player player = info.getPlayer();
				tempMap.put("player",player.getBasicInfoMap());
				tempMap.put("type", type);
				switch (type) {
					case 1: //ready
						if(player.getIsReady() == true) continue outer;
						player.setIsReady(true);
						this.readyCounter += 1;
						if(this.readyCounter == this.maxPlayer && players.size() >= 2) break outer;
						break;
					case 2: //leave
						leavePlayer(player);
						player.getHall().joinPlayer(player, getRoomTypeID());
						break;
					case 3: //chat
						tempMap.put("msg", map.get("msg"));
						break;
					default:
						continue outer;
				}
				String msg = JSON.toJSONString(tempMap);
				for(Player p : players){
					p.sendMsg(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void gameStart() {
		this.readyCounter = 0;
		System.out.println("[INFO]Room " + roomID + " start.");
		for(Player p : players) {
			p.setIsReady(false);
		}
		synchronized(this){
			this.status = RoomStatus.RUNNING;
		}
	}
	public abstract void runRunning();
	public void gameEnd(){
		synchronized(this){
			status = RoomStatus.WATING;
		}
	}
	
	@Override
	public void run() {
		System.out.println("[INFO]Room " + roomID + " is running now.");
		while(true) {
			runWaiting();
			gameStart();
			runRunning();
			gameEnd();
		}
	}	

	public synchronized Map<String, Object> getRoomInfoMap(){
		Map<String, Object> roomJsonMap = new HashMap<>();
		roomJsonMap.put("id", Integer.valueOf(roomID));
		roomJsonMap.put("status", status);
		roomJsonMap.put("playerCount", Integer.valueOf(playerCounter));
		roomJsonMap.put("maxPlayer", Integer.valueOf(maxPlayer));

		return roomJsonMap;
	}

	public Map<String, Object> getPlayersStatus() {
		/* All in room thread, no need to lock */
		ArrayList<Map<String, Object>> maps = new ArrayList<>(); 
		for(Player player: players){
			Map<String, Object> map = new HashMap<>();
			map.put("name", player.getName());
			map.put("avatarID", player.getAvatarID());
			map.put("isReady", player.getIsReady());
			maps.add(map);
		}
		Map<String, Object> map2 = new HashMap<>();
		map2.put("roomType", getRoomTypeID());
		map2.put("roomID", roomID);
		map2.put("players", maps);
		return map2;
	}
	
	public RoomStatus getStatus() {
		return status;
	}
	public LinkedList<Player> getPlayers() {
		return players;
	}
}

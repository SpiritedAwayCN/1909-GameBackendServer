package room;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import room.player.*;

public abstract class GameRoom implements Runnable{
	public enum RoomStatus{
		WATING, RUNNING
	}
	
	int roomID;
	LinkedList<Player> players = new LinkedList<Player>();
	public BlockingQueue<Info> infoQueue = new ArrayBlockingQueue<Info>(20);
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
	
	public abstract boolean joinPlayer(Player p);
	public abstract boolean leavePlayer(Player p);
	
	protected void leavePlayerWhileWaiting(Player p) {
		if(p.getIsReady())
			readyCounter -= 1;
		for(Player player: players) {
			player.sendMsg("Player " + p.getId() + " has left the room.");
		}
	}
	
	public void runWaiting() {
		while(true) {
			Info info = null;
			try {
				info = infoQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
				continue;
			}
			
			if (info.getMsgString().equals("join")) {
				Player p = info.getPlayer();
				if(p.getRoom() == this) {
					p.sendMsg("Already Entered");
					continue;
				}else if(!joinPlayer(p)){
					try {
						PrintWriter out = new java.io.PrintWriter(p.client_socket.getOutputStream());
						out.println("Fail to join, full");
						p.client_socket.close();
					} catch (IOException e) {}
					continue;
				}
				StringBuilder sb = new StringBuilder("Welcome to Room " + roomID
						+ ", you are Player " + p.getId() + ":\n");
				for(Player player: players) {
					player.sendMsg("Player " + p.getId() + " has entered the room.");
					sb.append("Player " + player.getId() + "\t\t" + (player.getIsReady() ? "Ready" : "N") + "\n");
				}
				p.sendMsg(sb.toString());
				continue;
			}
			
			if (info.getMsgString().equals("L!E@A#V$E%")) {
				info.getPlayer().onDisconnect();
				continue;
			}
			
			if(!info.getPlayer().getIsReady() && info.getMsgString().equalsIgnoreCase("ready")) {
				Player player = info.getPlayer();
				player.setIsReady(true);
				this.readyCounter += 1;
				if(this.readyCounter == this.maxPlayer)
					break;
				for(Player p : players) {
					p.sendMsg("Player " + player.getId() + " ready!");
				}
			}
		}
		System.out.println("[INFO]Room " + roomID + " start.");
		for(Player p : players) {
			p.setIsReady(false);
			p.sendMsg("All ready, game start!");
		}
		this.readyCounter = 0;
	}
	
	public void gameStart() {
		this.status = RoomStatus.RUNNING;
	}
	public abstract void runRunning();
	public abstract void gameEnd();
	
	@Override
	public void run() {
		while(true) {
			runWaiting();
			gameStart();
			runRunning();
			gameEnd();
		}
	}
	
	public RoomStatus getStatus() {
		return status;
	}
	public LinkedList<Player> getPlayers() {
		return players;
	}
}

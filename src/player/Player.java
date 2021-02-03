package player;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSON;

import main.GameHall;
import room.GameRoom;

public class Player {
	public Socket client_socket;
	protected BufferedReader in;
	protected PrintWriter out;
	
	protected BlockingQueue<String> sendQueue;
	protected BlockingQueue<Info> recvQueue;
	
	protected int id = 0;
	protected boolean isReady;
	protected GameRoom room;
	protected GameHall hall;

	protected String name;
	protected int avatarID;
	protected Map<String, Object> basicInfoMap;

	private Thread listenThread;
	private Thread sendThread;
	public Player(){}
	public Player(Socket socket, GameHall hall){
		this.client_socket = socket;
		this.hall = hall;
		listenThread = new Thread(()->{
			while(true) {
				String msg = new String();
				try {
					msg = in.readLine();
					System.out.println("R: " + msg);
				} catch(IOException e) {
				// 	e.printStackTrace(); //SocketException?
					break;
				} 
				if(msg == null) break;
				try {
					recvQueue.put(new Info(this, msg));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				recvQueue.put(new Info(this, "L!E@A#V$E%"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		sendThread = new Thread(()->{
			while (true) {
				try {
					String msg = sendQueue.take();
					System.out.println("S:" + msg);
					out.println(msg);
					out.flush();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
			try {
				room.infoQueue.put(new Info(this, "L!E@A#V$E%"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
	}
	public boolean openStream() {
		int fetchID = 0;
		try {
            in = new BufferedReader(new InputStreamReader(client_socket.getInputStream(), "UTF-8"));
			out = new java.io.PrintWriter(new OutputStreamWriter(client_socket.getOutputStream(), "UTF-8"));
			sendQueue = new ArrayBlockingQueue<String>(20);

			out.println("0.0.1");
			out.flush();
			// client_socket.setSoTimeout(30000);
			String greetingString = in.readLine();
			System.out.println("R: " + greetingString);
			// client_socket.setSoTimeout(0);
			Map<String, Object> playerInfoMap = JSON.parseObject(greetingString), infoMap = new HashMap<>();
			this.name = (String)playerInfoMap.get("name");
			this.avatarID = (int)playerInfoMap.get("avatarID");
			fetchID = (int)playerInfoMap.get("fetch");

			infoMap.put("name", this.name);
			infoMap.put("avatarID", this.avatarID);
			this.basicInfoMap = infoMap;

        } catch (Exception e) {
            try { client_socket.close(); } catch (IOException e2) { ; }
            e.printStackTrace();
            return false;
		}

		this.hall.joinPlayer(this, fetchID);
		sendThread.start();
		listenThread.start();
		return true;
	}
	
	public boolean sendMsg(String msg) {
		try {
			sendQueue.put(msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public synchronized void onDisconnect() {
		if(client_socket.isClosed()) return;
		try { client_socket.close(); } catch (IOException e2) { ; }
		if(room != null) 
			room.leavePlayer(this);
		else if(recvQueue != null)
			hall.leavePlayer(this);
	}

	public void setRecvQueue(BlockingQueue<Info> recvQueue) {
		this.recvQueue = recvQueue;
	}
	
	public String getName() {
		return name;
	}
	public GameRoom getRoom() {
		return room;
	}
	public int getId() {
		return id;
	}
	public boolean getIsReady() {
		return isReady;
	}
	public int getAvatarID() {
		return avatarID;
	}
	public Map<String, Object> getBasicInfoMap() {
		return basicInfoMap;
	}
	public GameHall getHall() {
		return hall;
	}
	public void setIsReady(boolean isReady) {
		this.isReady = isReady;
	}
	public void setRoom(GameRoom room) {
		this.room = room;
	}
	public void setId(int id) {
		this.id = id;
	}
}

package player;

import java.io.*;
import java.net.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import room.GameRoom;

public class Player {
	public Socket client_socket;
	protected BufferedReader in;
	protected PrintWriter out;
	
	BlockingQueue<String> sendQueue;
	
	int id = -1;
	boolean isReady;
	GameRoom room;
	String name;
	public Thread listenThread;
	public Thread sendThread;
	public Player(){}
	public Player(Socket socket, String name){
		this.client_socket = socket;
		this.name = name;
		listenThread = new Thread(()->{
			while(true) {
				String msg = new String();
				try {
					msg = in.readLine();
				} catch(IOException e) {
				// 	e.printStackTrace(); //SocketException?
					break;
				} 
				if(msg == null) break;
				try {
					room.infoQueue.put(new Info(this, msg));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			try {
				room.infoQueue.put(new Info(this, "L!E@A#V$E%"));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		sendThread = new Thread(()->{
			while (true) {
				try {
					String msg = sendQueue.take();
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
		try {
            in = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
			out = new java.io.PrintWriter(client_socket.getOutputStream());
			sendQueue = new ArrayBlockingQueue<String>(20);
        }
        catch (IOException e) {
            try { client_socket.close(); } catch (IOException e2) { ; }
            e.printStackTrace();
            return false;
        }
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
		// TODO 
		if(client_socket.isClosed()) return;
		try { client_socket.close(); } catch (IOException e2) { ; }
		if(room != null) 
			room.leavePlayer(this);
		
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

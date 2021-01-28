package main;

import java.io.*;
import java.net.*;

import room.GameRoom;
import room.GobangRoom;
import player.Info;
import player.Player;

public class ServerMain implements Runnable{
	private static final int PORT = 25565;
	protected ServerSocket listen_socket;
	protected Thread listenThread, tempRoomThread;
	protected GameRoom tempRoom;

	public static void main(String[] args) {
		ServerMain server = new ServerMain();
		try {
			server.listen_socket = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Server: listening on port " + PORT);
		server.tempRoom = new GobangRoom(1);
		server.tempRoomThread = new Thread(server.tempRoom);
		server.listenThread = new Thread(server);
		server.tempRoomThread.start();
		server.listenThread.start();
	}

	@Override
	public void run() {
		while(true) {
			try {
                Socket client_socket = listen_socket.accept();
                Player player = new Player(client_socket, "unamed");
                tempRoom.infoQueue.put(new Info(player, "join"));
            }
			catch (Exception e) {
                 e.printStackTrace();
			}
		}
	}

}

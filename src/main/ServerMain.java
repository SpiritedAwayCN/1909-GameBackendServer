package main;

import java.io.*;
import java.net.*;

import player.Player;

public class ServerMain implements Runnable{
	private static final int PORT = 25565;
	protected ServerSocket listen_socket;
	protected Thread listenThread, hallThread;
	protected GameHall gameHall;

	public static void main(String[] args) {
		ServerMain server = new ServerMain();
		try {
			server.listen_socket = new ServerSocket(PORT);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Server: listening on port " + PORT);
		server.gameHall = new GameHall();
		server.hallThread = new Thread(server.gameHall);
		server.hallThread.start();
		server.listenThread = new Thread(server);
		server.listenThread.start();
	}

	@Override
	public void run() {
		while(true) {
			try {
                Socket client_socket = listen_socket.accept();
                final Player player = new Player(client_socket, gameHall);
                new Thread(()->{
					player.openStream();
				}).start();
            }
			catch (Exception e) {
                 e.printStackTrace();
			}
		}
	}

}

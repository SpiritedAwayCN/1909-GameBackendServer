package main;

import java.util.*;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSON;

import player.*;
import room.*;

public class GameHall implements Runnable {
    private static final int[] MAXROOM = { -1, 8, 4 };
    private int hallId;
    ArrayList<GameRoom> gobangRooms; // 1

    LinkedList<Player> players = new LinkedList<>();
    BlockingQueue<Info> infoQueue = new ArrayBlockingQueue<>(100);
    LinkedList<Thread> roomThreads = new LinkedList<>();

    public GameHall() {
        hallId = 0;
        gobangRooms = new ArrayList<>();
        gameHallInit();
    }

    public void gameHallInit() {
        for (int i = 1; i <= MAXROOM[1]; i++) {
            GameRoom room = new GobangRoom(i);
            gobangRooms.add(room);
            Thread thread = new Thread(room);
            roomThreads.add(thread);
            thread.start();
        }
    }

    private ArrayList<GameRoom> getRoomListByID(int id) {
        switch (id) {
            case GobangRoom.roomTypeID:
                return gobangRooms;
        }
        return gobangRooms;
    }

    public Map<String, Object> getRoomListMap(int typeID, int roomID) {
        ArrayList<Map<String, Object>> maps = new ArrayList<>();
        Map<String, Object> roomListMap = new HashMap<>();
        ArrayList<GameRoom> gameRooms = getRoomListByID(typeID);

        if (roomID == 0) {
            for (GameRoom room : gameRooms) {
                maps.add(room.getRoomInfoMap());
            }
        } else {
            try {
                maps.add(gameRooms.get(roomID - 1).getRoomInfoMap());
            } catch (Exception e) {
                for (GameRoom room : gameRooms) {
                    maps.add(room.getRoomInfoMap());
                }
            }
        }

        roomListMap.put("type", Integer.valueOf(typeID));
        roomListMap.put("total", Integer.valueOf(gameRooms.size()));
        roomListMap.put("rooms", maps);

        return roomListMap;
    }

    public BlockingQueue<Info> getInfoQueue() {
        return infoQueue;
    }

    public int getHallId() {
        return hallId;
    }

    @Override
    public void run() {
        System.out.println("Hall is running.");
        while (true) {
            Info info = null;
            try {
                info = infoQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                continue;
            }

            // info.getPlayer().sendMsg(info.getMsgString());
            if (info.getMsgString().equals("L!E@A#V$E%")) {
				info.getPlayer().onDisconnect();
				continue;
            }
            
            int opType, roomType, roomID;
            try {
                Map<String, Object> infoMap = JSON.parseObject(info.getMsgString());
                opType = (int) infoMap.get("opType");
                roomType = (int) infoMap.get("roomType");
                roomID = (int) infoMap.get("roomID");
            } catch (Exception e) {
                continue;
            }
            switch (opType) {
                case 0:
                    info.getPlayer().sendMsg(JSON.toJSONString(getRoomListMap(roomType, roomID)));
                    break;
                case 1:
                    final GameRoom gameRoom = getRoomListByID(roomType).get(roomID - 1);
                    final Player player = info.getPlayer();
                    new Thread(() -> {
                        try {
                            gameRoom.infoQueue.put(new Info(player, "join"));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();
                    break;
                default:
                    break;
            }
        }

    }

    protected void addPlayers(Player p){
        synchronized(players){
            players.add(p);
        }
    }

    protected void removePlayers(Player p){
        synchronized(players){
            players.remove(p);
        }
    }

    public void joinPlayer(Player p, int tid){
        p.setRecvQueue(this.infoQueue);
        this.addPlayers(p);
        p.sendMsg(JSON.toJSONString(getRoomListMap(tid, 0)));
    }

    public void leavePlayer(Player p){
        removePlayers(p);
    }
}

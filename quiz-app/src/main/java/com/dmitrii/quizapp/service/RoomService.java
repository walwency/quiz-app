package com.dmitrii.quizapp.service;

import com.dmitrii.quizapp.model.Player;
import com.dmitrii.quizapp.model.Room;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RoomService {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room createRoom(String hostSessionId) {
        String code = generateRoomCode();
        Room room = new Room(code);
        room.setHostSessionId(hostSessionId);
        rooms.put(code, room);
        return room;
    }

    public Room getRoom(String code) {
        return rooms.get(code);
    }

    public Player addPlayerToRoom(String roomCode, String sessionId, String playerName) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomCode);
        }
        Player player = new Player(sessionId, playerName);
        room.getPlayers().put(sessionId, player);
        return player;
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
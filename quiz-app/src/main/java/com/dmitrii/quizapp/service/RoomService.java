package com.dmitrii.quizapp.service;

import com.dmitrii.quizapp.model.Player;
import com.dmitrii.quizapp.model.Question;
import com.dmitrii.quizapp.model.Room;
import com.dmitrii.quizapp.model.RoomStatus;
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
        Room room = getRoomOrThrow(roomCode);
        Player player = new Player(sessionId, playerName);
        room.getPlayers().put(sessionId, player);
        return player;
    }

    public void addQuestion(String roomCode, Question question) {
        Room room = getRoomOrThrow(roomCode);
        room.getQuestions().add(question);
    }

    public void startGame(String roomCode) {
        Room room = getRoomOrThrow(roomCode);
        if (room.getQuestions().isEmpty()) {
            throw new IllegalStateException("Cannot start game without questions");
        }
        room.setStatus(RoomStatus.IN_PROGRESS);
        room.setCurrentQuestionIndex(0);
        room.setCurrentQuestionStartTime(System.currentTimeMillis());
    }

    private Room getRoomOrThrow(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            throw new IllegalArgumentException("Room not found: " + roomCode);
        }
        return room;
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
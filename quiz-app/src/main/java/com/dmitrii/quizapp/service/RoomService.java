package com.dmitrii.quizapp.service;

import com.dmitrii.quizapp.dto.AnswerResult;
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
    public AnswerResult submitAnswer(String roomCode, String sessionId, int selectedOptionIndex) {
        Room room = getRoomOrThrow(roomCode);
        Player player = room.getPlayers().get(sessionId);
        if (player == null) {
            throw new IllegalArgumentException("Player not found in room");
        }

        int questionIndex = room.getCurrentQuestionIndex();
        if (player.getAnsweredQuestionIndexes().contains(questionIndex)) {
            throw new IllegalStateException("Already answered this question");
        }
        player.getAnsweredQuestionIndexes().add(questionIndex);

        Question question = room.getCurrentQuestion();
        boolean isCorrect = question.getCorrectOptionIndex() == selectedOptionIndex;

        int pointsEarned = 0;
        if (isCorrect) {
            pointsEarned = calculatePoints(room.getCurrentQuestionStartTime());
            player.setScore(player.getScore() + pointsEarned);
        }

        return new AnswerResult(player.getName(), isCorrect, pointsEarned, player.getScore());
    }

    private int calculatePoints(long questionStartTime) {
        long elapsedMs = System.currentTimeMillis() - questionStartTime;
        int maxPoints = 1000;
        int minPoints = 100;
        long maxTimeMs = 10_000; // 10 секунд на ответ

        if (elapsedMs >= maxTimeMs) {
            return minPoints;
        }

        // чем быстрее ответил, тем больше очков, линейно от maxPoints до minPoints
        double ratio = 1.0 - ((double) elapsedMs / maxTimeMs);
        return (int) (minPoints + (maxPoints - minPoints) * ratio);
    }
}
package com.dmitrii.quizapp.websocket;

import com.dmitrii.quizapp.dto.AnswerResult;
import com.dmitrii.quizapp.dto.IncomingMessage;
import com.dmitrii.quizapp.dto.OutgoingMessage;
import com.dmitrii.quizapp.model.Player;
import com.dmitrii.quizapp.model.Question;
import com.dmitrii.quizapp.model.Room;
import com.dmitrii.quizapp.service.RoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class QuizWebSocketHandler extends TextWebSocketHandler {

    private final RoomService roomService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    public QuizWebSocketHandler(RoomService roomService) {
        this.roomService = roomService;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        IncomingMessage incoming = objectMapper.readValue(message.getPayload(), IncomingMessage.class);

        switch (incoming.getType()) {
            case "CREATE_ROOM" -> handleCreateRoom(session);
            case "JOIN_ROOM" -> handleJoinRoom(session, incoming);
            case "ADD_QUESTION" -> handleAddQuestion(session, incoming);
            case "START_GAME" -> handleStartGame(session, incoming);
            case "SUBMIT_ANSWER" -> handleSubmitAnswer(session, incoming);
            default -> sendError(session, "Unknown message type: " + incoming.getType());
        }
    }

    private void handleCreateRoom(WebSocketSession session) throws IOException {
        Room room = roomService.createRoom(session.getId());
        activeSessions.put(session.getId(), session);

        send(session, new OutgoingMessage("ROOM_CREATED", Map.of("roomCode", room.getCode())));
    }

    private void handleJoinRoom(WebSocketSession session, IncomingMessage incoming) throws IOException {
        try {
            Player player = roomService.addPlayerToRoom(
                    incoming.getRoomCode(), session.getId(), incoming.getPlayerName());
            activeSessions.put(session.getId(), session);

            Room room = roomService.getRoom(incoming.getRoomCode());

            broadcastToRoom(room, new OutgoingMessage("PLAYER_JOINED", Map.of(
                    "playerName", player.getName(),
                    "totalPlayers", room.getPlayers().size()
            )));
        } catch (IllegalArgumentException e) {
            sendError(session, e.getMessage());
        }
    }

    private void broadcastToRoom(Room room, OutgoingMessage message) throws IOException {

        for (Player player : room.getPlayers().values()) {
            sendIfOpen(player.getSessionId(), message);
        }

        sendIfOpen(room.getHostSessionId(), message);
    }

    private void sendIfOpen(String sessionId, OutgoingMessage message) throws IOException {
        WebSocketSession session = activeSessions.get(sessionId);
        if (session != null && session.isOpen()) {
            send(session, message);
        }
    }

    private void send(WebSocketSession session, OutgoingMessage message) throws IOException {
        String json = objectMapper.writeValueAsString(message);
        session.sendMessage(new TextMessage(json));
    }

    private void sendError(WebSocketSession session, String errorText) throws IOException {
        send(session, new OutgoingMessage("ERROR", Map.of("message", errorText)));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        activeSessions.remove(session.getId());
    }
    private void handleAddQuestion(WebSocketSession session, IncomingMessage incoming) throws IOException {
        try {
            Question question = new Question();
            question.setText(incoming.getQuestionText());
            question.setOptions(incoming.getOptions());
            question.setCorrectOptionIndex(incoming.getCorrectOptionIndex());

            roomService.addQuestion(incoming.getRoomCode(), question);

            send(session, new OutgoingMessage("QUESTION_ADDED", Map.of(
                    "questionText", question.getText()
            )));
        } catch (IllegalArgumentException e) {
            sendError(session, e.getMessage());
        }
    }
    private void handleSubmitAnswer(WebSocketSession session, IncomingMessage incoming) throws IOException {
        try {
            AnswerResult result = roomService.submitAnswer(
                    incoming.getRoomCode(), session.getId(), incoming.getSelectedOptionIndex());

            send(session, new OutgoingMessage("ANSWER_RESULT", Map.of(
                    "correct", result.correct(),
                    "pointsEarned", result.pointsEarned(),
                    "totalScore", result.totalScore()
            )));

            Room room = roomService.getRoom(incoming.getRoomCode());
            sendIfOpen(room.getHostSessionId(), new OutgoingMessage("PLAYER_ANSWERED", Map.of(
                    "playerName", result.playerName()
            )));
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleStartGame(WebSocketSession session, IncomingMessage incoming) throws IOException {
        try {
            roomService.startGame(incoming.getRoomCode());
            Room room = roomService.getRoom(incoming.getRoomCode());

            broadcastCurrentQuestion(room);
        } catch (IllegalArgumentException | IllegalStateException e) {
            sendError(session, e.getMessage());
        }
    }

    private void broadcastCurrentQuestion(Room room) throws IOException {
        Question question = room.getCurrentQuestion();

        broadcastToRoom(room, new OutgoingMessage("NEW_QUESTION", Map.of(
                "questionText", question.getText(),
                "options", question.getOptions(),
                "questionIndex", room.getCurrentQuestionIndex()
        )));
    }
}
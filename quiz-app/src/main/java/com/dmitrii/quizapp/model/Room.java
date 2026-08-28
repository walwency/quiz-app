package com.dmitrii.quizapp.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Room {
    private String code;
    private String hostSessionId;
    private Map<String, Player> players = new ConcurrentHashMap<>();
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = -1;
    private RoomStatus status = RoomStatus.WAITING;
    private long currentQuestionStartTime;

    public Room(String code) {
        this.code = code;
    }

    public Question getCurrentQuestion() {
        if (currentQuestionIndex < 0 || currentQuestionIndex >= questions.size()) {
            return null;
        }
        return questions.get(currentQuestionIndex);
    }
}
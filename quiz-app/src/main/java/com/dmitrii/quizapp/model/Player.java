package com.dmitrii.quizapp.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Player {
    private String sessionId;
    private String name;
    private int score;
    private Set<Integer> answeredQuestionIndexes = new HashSet<>();

    public Player(String sessionId, String name) {
        this.sessionId = sessionId;
        this.name = name;
        this.score = 0;
    }
}
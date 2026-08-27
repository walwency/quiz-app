package com.dmitrii.quizapp.model;

import lombok.Data;

@Data
public class Player {
    private String sessionId;
    private String name;
    private int score;

    public Player(String sessionId, String name) {
        this.sessionId = sessionId;
        this.name = name;
        this.score = 0;
    }


}

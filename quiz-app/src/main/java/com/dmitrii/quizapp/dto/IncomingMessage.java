package com.dmitrii.quizapp.dto;

import lombok.Data;

import java.util.List;

@Data
public class IncomingMessage {
    private String type;
    private String roomCode;
    private String playerName;

    private String questionText;
    private List<String> options;
    private Integer correctOptionIndex;
}
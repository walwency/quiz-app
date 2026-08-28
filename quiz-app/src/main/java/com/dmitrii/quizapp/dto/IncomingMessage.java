package com.dmitrii.quizapp.dto;

import lombok.Data;

@Data
public class IncomingMessage {
    private String type;
    private String roomCode;
    private String playerName;
}
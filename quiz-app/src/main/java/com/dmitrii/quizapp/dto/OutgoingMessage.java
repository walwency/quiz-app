package com.dmitrii.quizapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class OutgoingMessage {
    private String type;
    private Map<String, Object> payload;
}
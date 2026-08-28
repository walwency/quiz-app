package com.dmitrii.quizapp.dto;

public record AnswerResult(String playerName, boolean correct, int pointsEarned, int totalScore) {

}
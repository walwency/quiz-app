package com.dmitrii.quizapp.model;

import lombok.Data;

import java.util.List;
@Data
public class Question {
    private String text;
    private List<String> options;
    private int correctOptionIndex;

}

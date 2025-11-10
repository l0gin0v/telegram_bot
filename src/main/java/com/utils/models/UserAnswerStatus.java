package com.utils.models;

public class UserAnswerStatus {
    public boolean isCorrectAnswer;
    public String message;
    public boolean isQuit;

    public UserAnswerStatus(boolean isCorrectAnswer, String message, boolean isQuit) {
        this.isCorrectAnswer = isCorrectAnswer;
        this.message = message;
        this.isQuit = isQuit;
    }
}

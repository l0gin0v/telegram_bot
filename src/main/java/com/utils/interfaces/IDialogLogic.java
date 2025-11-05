package com.utils.interfaces;

import com.utils.models.UserAnswerStatus;

public interface IDialogLogic {
    String getQuestion();
    String needToStart();
    String welcomeWords();
    UserAnswerStatus processAnswer(String answer);
}

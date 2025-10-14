package com.utils.interfaces;

import com.utils.models.UserAnswerStatus;

public interface IDialogLogic {
    String getQuestion();
    String welcomeWords();
    String farewellWords();
    UserAnswerStatus processAnswer(String answer);
}

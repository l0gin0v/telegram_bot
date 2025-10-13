package com.utils.interfaces;

public interface IDialogLogic {
    IQuestion getQA();
    boolean checkAnswer(String answer);
    String getHelp();
}

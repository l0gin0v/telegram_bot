package com.utils.services;

import com.utils.interfaces.IDialogLogic;
import com.utils.interfaces.IQuestion;
import com.utils.interfaces.IQuestionRepository;
import com.utils.models.UserAnswerStatus;

import java.util.Objects;

public class DialogLogic implements IDialogLogic {
    IQuestionRepository questionRepository;
    IQuestion currentQA;

    public DialogLogic(IQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public String getQuestion() {
        this.currentQA = questionRepository.getRandomQuestion();
        return "\nВопрос: " + currentQA.getQuestion();
    }

    public String welcomeWords() {
        return "Добро пожаловать в бота!\n" +
                "==========================\n" +
                "Доступные команды:\n" +
                "  /help - получить справку\n" +
                "===========================\n";
    }

    public String farewellWords() {
        return "До свидания! Возвращайтесь еще!";
    }

    public UserAnswerStatus processAnswer(String answer) {
        if (answer.equals("/help")) {
            return new UserAnswerStatus(false, getHelp());
        }
        else if (currentQA.getAnswer().equals(answer))
            return new UserAnswerStatus(true, "Правильно! Отличная работа!");
        else
            return new UserAnswerStatus(false,
                    "Неправильно. Попробуйте еще раз или введите /quit");
    }

    private String getHelp() {
        return "Это бот для курса ООП - его я вам дам\n" +
                "А это справочка для бота - её я вам не дам...";
    }
}

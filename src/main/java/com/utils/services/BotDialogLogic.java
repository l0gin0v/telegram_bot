package com.utils.services;

import com.utils.interfaces.IDialogLogic;
import com.utils.interfaces.IQuestion;
import com.utils.interfaces.IQuestionRepository;
import com.utils.models.UserAnswerStatus;

public class BotDialogLogic implements IDialogLogic {
    IQuestionRepository questionRepository;
    IQuestion currentQuestionInfo;

    public BotDialogLogic(IQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public String getQuestion() {
        this.currentQuestionInfo = questionRepository.getRandomQuestion();
        return "\nВопрос: " + currentQuestionInfo.getQuestion();
    }

    public String needToStart() {
        return "Для запуска бота введите /start";
    }

    public String welcomeWords() {
        return "Добро пожаловать в бота!\n" +
                "==========================\n" +
                "Доступные команды:\n" +
                "  /help - получить справку\n" +
                "  /quit - выйти из бота\n" +
                "===========================\n";
    }

    private String farewellWords() {
        return "До свидания! Возвращайтесь еще!";
    }

    public UserAnswerStatus processAnswer(String answer) {
        if (answer.equals("/help")) {
            return new UserAnswerStatus(false, getHelp(), false);
        }
        else if (answer.equals("/quit")) {
            return new UserAnswerStatus(false, farewellWords(), true);
        }
        else if (currentQuestionInfo.getAnswer().equals(answer))
            return new UserAnswerStatus(true, "Правильно! Отличная работа!", false);
        else
            return new UserAnswerStatus(false,
                    "Неправильно. Попробуйте еще раз или введите /quit", false);
    }

    private String getHelp() {
        return "Это бот для курса ООП - его я вам дам\n" +
                "А это справочка для бота - её я вам не дам...";
    }
}

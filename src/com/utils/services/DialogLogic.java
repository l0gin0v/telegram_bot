package src.com.utils.services;

import src.com.utils.interfaces.IDialogLogic;
import src.com.utils.interfaces.IQuestion;
import src.com.utils.interfaces.IQuestionRepository;

import java.util.Objects;

public class DialogLogic implements IDialogLogic {
    IQuestionRepository questionRepository;
    IQuestion currentQA;

    public DialogLogic(IQuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public IQuestion getQA() {
        return (this.currentQA = questionRepository.getRandomQuestion());
    }

    public boolean checkAnswer(String answer) {
        if (currentQA == null) {
            return false;
        }
        return (Objects.equals(currentQA.getAnswer(), answer));
    }

    public String getHelp() {
        return "Это бот для курса ООП - его я вам дам\n" + "А это справочка для бота - её я вам не дам...";
    }
}

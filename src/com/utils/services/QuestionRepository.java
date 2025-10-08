package src.com.utils.services;

import src.com.utils.interfaces.IQuestionRepository;
import src.com.utils.interfaces.IQuestion;
import src.com.utils.models.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuestionRepository implements IQuestionRepository {
    private final List<Question> listOfQuestion;

    public QuestionRepository() {
        this.listOfQuestion = new ArrayList<>();
        initializeQuestions();
    }

    private void initializeQuestions() {
        listOfQuestion.add(new Question(1, "7 + 7", "14"));
        listOfQuestion.add(new Question(2, "1 + 2", "3"));
        listOfQuestion.add(new Question(3, "50 + 5", "55"));
        listOfQuestion.add(new Question(4, "14 + 7", "21"));
    }

    @Override
    public Question getRandomQuestion() {
        Random random = new Random();
        int randomIndex = random.nextInt(listOfQuestion.size());

        return listOfQuestion.get(randomIndex);
    }
}

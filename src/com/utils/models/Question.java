package src.com.utils.models;

import src.com.utils.interfaces.IQuestion;

public class Question implements IQuestion {
    private String question;
    private String answer;
    private Integer id;

    public Question(int id, String question, String answer) {
    }

    //Getter for question
    public String getQuestion() {
        return question;
    }

    //Getter for answer
    public String getAnswer() {
        return answer;
    }

    public Integer getId() {
        return id;
    }
}

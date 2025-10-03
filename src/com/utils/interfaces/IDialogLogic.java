package src.com.utils.interfaces;

public interface IDialogLogic {
    IQuestion getQA();
    boolean checkAnswer(int answer);
    String getHelp();
}

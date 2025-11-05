package com.utils.services;

import com.utils.interfaces.IConsole;
import com.utils.interfaces.IDialogLogic;
import com.utils.models.UserAnswerStatus;
import java.util.Scanner;

public class Console implements IConsole {
    private final IDialogLogic dialogLogic;
    private final Scanner scanner;
    private boolean isRunning;

    public Console(IDialogLogic dialogLogic) {
        this.dialogLogic = dialogLogic;
        this.scanner = new Scanner(System.in);
        this.isRunning = false;
    }

    private void start() {
        isRunning = true;
        System.out.println(dialogLogic.welcomeWords());
    }

    public void runBot() {
        while (!scanner.nextLine().trim().equals("/start")) {
            System.out.println(dialogLogic.needToStart());
        }

        start();

        while (isRunning) {
            System.out.println(dialogLogic.getQuestion());

            boolean questionAnswered = false;

            UserAnswerStatus userAnswerStatus;

            while (!questionAnswered && isRunning) {
                System.out.print("Ваш ответ: ");
                String userInput = scanner.nextLine().trim();
                userAnswerStatus = dialogLogic.processAnswer(userInput);
                System.out.println(userAnswerStatus.message());
                questionAnswered = userAnswerStatus.isCorrectAnswer();
                isRunning = !userAnswerStatus.isQuit();
            }
        }
    }
}
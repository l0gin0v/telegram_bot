package com.utils.services;

import com.utils.interfaces.IConsole;
import com.utils.interfaces.IDialogLogic;
import com.utils.interfaces.IQuestion;
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

    @Override
    public void start() {
        isRunning = true;

        System.out.println("Добро пожаловать в бота!");
        System.out.println("==========================");
        System.out.println("Доступные команды:");
        System.out.println("  - help - получить справку");
        System.out.println("===========================");

        while (isRunning) {
            runQuizCycle();
        }

        scanner.close();
        System.out.println("До свидания! Возвращайтесь еще!");
    }

    @Override
    public void runQuizCycle() {
        IQuestion question = dialogLogic.getQA();
        System.out.println("\nВопрос: " + question.getQuestion());

        boolean questionAnswered = false;

        while (!questionAnswered && isRunning) {
            System.out.print("Ваш ответ: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase("help")) {
                String help = dialogLogic.getHelp();
                System.out.println(help);
                continue;
            }

            if (dialogLogic.checkAnswer(userInput)) {
                System.out.println("Правильно! Отличная работа!");
                questionAnswered = true;
            } else {
                System.out.println("Неправильно. Попробуйте еще раз или введите ");
            }
        }
    }

    @Override
    public void stop() {
        isRunning = false;
    }
}
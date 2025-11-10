package com.utils.tests;

import com.utils.interfaces.IDialogLogic;
import com.utils.models.UserAnswerStatus;
import com.utils.services.Console;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsoleTest {

    @Mock
    private IDialogLogic dialogLogic;

    private Console console;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    void runBot_ShowsWelcomeAndQuestion() throws InterruptedException {
        // Arrange
        String input = "/start\n/quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        when(dialogLogic.welcomeWords()).thenReturn("Добро пожаловать!");
        when(dialogLogic.getQuestion()).thenReturn("Вопрос 1");
        when(dialogLogic.processAnswer("/quit")).thenReturn(new UserAnswerStatus(false, "Пока!", true));

        console = new Console(dialogLogic);

        // Act
        Thread botThread = new Thread(() -> console.runBot());
        botThread.start();
        botThread.join(1000); // Ждем завершения

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Добро пожаловать!"));
        assertTrue(output.contains("Вопрос 1"));
        assertTrue(output.contains("Пока!"));
    }

    @Test
    void runBot_ProcessesCorrectAnswer() throws InterruptedException {
        // Arrange
        String input = "/start\nответ\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question");
        when(dialogLogic.processAnswer("ответ")).thenReturn(new UserAnswerStatus(true, "Правильно!", false));

        console = new Console(dialogLogic);

        // Act
        Thread botThread = new Thread(() -> console.runBot());
        botThread.start();
        Thread.sleep(100);
        botThread.interrupt(); // Прерываем после первого вопроса

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Ваш ответ:"));
        assertTrue(output.contains("Правильно!"));
    }

    @Test
    void runBot_WaitsForStartCommand() throws InterruptedException {
        // Arrange
        String input = "wrong\ninvalid\n/start\n/quit\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        when(dialogLogic.needToStart()).thenReturn("Введите /start");
        when(dialogLogic.welcomeWords()).thenReturn("Started");
        when(dialogLogic.getQuestion()).thenReturn("Q");
        when(dialogLogic.processAnswer("/quit")).thenReturn(new UserAnswerStatus(false, "Bye", true));

        console = new Console(dialogLogic);

        // Act
        Thread botThread = new Thread(() -> console.runBot());
        botThread.start();
        botThread.join(1000);

        // Assert
        String output = outputStream.toString();
        assertTrue(output.contains("Введите /start"));
        verify(dialogLogic, atLeast(2)).needToStart(); // Должен вызываться для неправильных команд
    }
}
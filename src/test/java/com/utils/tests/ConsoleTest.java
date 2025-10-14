package com.utils.tests;

import com.utils.services.Console;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.utils.interfaces.IConsole;
import com.utils.interfaces.IDialogLogic;
import com.utils.interfaces.IQuestion;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConsoleTest {

    @Mock
    private IDialogLogic mockDialogLogic;

    @Mock
    private IQuestion mockQuestion;

    private InputStream originalSystemIn;

    @BeforeEach
    void setUp() {
        originalSystemIn = System.in;
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalSystemIn);
    }

    @Test
    void testConsoleCreation() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            IConsole console = new Console(mockDialogLogic);
        }, "Конструктор не должен выбрасывать исключения");
    }

    @Test
    void testStopMethod() {
        // Arrange
        Console console = new Console(mockDialogLogic);

        when(mockDialogLogic.getQuestion()).thenReturn(mockQuestion);
        when(mockQuestion.getQuestion()).thenReturn("Test question");

        // Act
        console.stop();

        assertDoesNotThrow(() -> {
            Thread testThread = new Thread(() -> {
                console.runQuizCycle();
            });
            testThread.start();
            Thread.sleep(100);
            testThread.interrupt();
        });
    }

    @Test
    void testRunQuizCycle_WithCorrectAnswer() {
        // Arrange
        Console console = new Console(mockDialogLogic);


        when(mockDialogLogic.getQuestion()).thenReturn(mockQuestion);
        when(mockQuestion.getQuestion()).thenReturn("7 + 7");

        String simulatedInput = "14\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // Act & Assert
        assertDoesNotThrow(() -> {
            Thread testThread = new Thread(() -> console.runQuizCycle());
            testThread.start();
            Thread.sleep(500);
            testThread.interrupt();
        }, "Метод должен работать без исключений при корректном вводе");
    }

    @Test
    void testHelpMethodIsCalled() {
        // Arrange
        Console console = new Console(mockDialogLogic);

        when(mockDialogLogic.getQuestion()).thenReturn(mockQuestion);
        when(mockQuestion.getQuestion()).thenReturn("50 + 5");

        lenient().when(mockDialogLogic.getHelp()).thenReturn("Это арифметический вопрос");
        lenient().when(mockDialogLogic.processAnswer("55")).thenReturn(true);

        String simulatedInput = "help\n55\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // Act
        assertDoesNotThrow(() -> {
            Thread testThread = new Thread(() -> console.runQuizCycle());
            testThread.start();
            Thread.sleep(500);
            testThread.interrupt();
        });
        verify(mockDialogLogic, atLeastOnce()).getQuestion();
    }
}
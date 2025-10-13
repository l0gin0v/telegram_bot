package com.utils.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.utils.interfaces.IQuestion;
import com.utils.interfaces.IQuestionRepository;
import com.utils.services.DialogLogic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DialogLogicTest {

    @Mock
    private IQuestionRepository questionRepository;

    @Mock
    private IQuestion question;

    private DialogLogic dialogLogic;

    @BeforeEach
    void setUp() {
        dialogLogic = new DialogLogic(questionRepository);
    }

    @Test
    void testGetQA_ShouldReturnQuestionFromRepository() {
        when(questionRepository.getRandomQuestion()).thenReturn(question);

        IQuestion result = dialogLogic.getQA();

        assertNotNull(result);
        assertEquals(question, result);
        verify(questionRepository, times(1)).getRandomQuestion();
    }

    @Test
    void testGetQA_ShouldSetCurrentQA() {
        when(questionRepository.getRandomQuestion()).thenReturn(question);

        IQuestion result = dialogLogic.getQA();

        assertNotNull(result);
        assertEquals(question, result);
    }

    @Test
    void testCheckAnswer_WhenAnswerIsCorrect_ShouldReturnTrue() {
        String correctAnswer = "правильный ответ";
        when(question.getAnswer()).thenReturn(correctAnswer);
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        dialogLogic.getQA();

        boolean result = dialogLogic.checkAnswer(correctAnswer);

        assertTrue(result);
        verify(question, times(1)).getAnswer();
    }

    @Test
    void testCheckAnswer_WhenAnswerIsIncorrect_ShouldReturnFalse() {
        String correctAnswer = "правильный ответ";
        String wrongAnswer = "неправильный ответ";
        when(question.getAnswer()).thenReturn(correctAnswer);
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        dialogLogic.getQA();

        boolean result = dialogLogic.checkAnswer(wrongAnswer);

        assertFalse(result);
        verify(question, times(1)).getAnswer();
    }

    @Test
    void testCheckAnswer_WhenAnswerIsNull_ShouldHandleCorrectly() {
        String correctAnswer = "правильный ответ";
        when(question.getAnswer()).thenReturn(correctAnswer);
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        dialogLogic.getQA();

        boolean result = dialogLogic.checkAnswer(null);

        assertFalse(result);
        verify(question, times(1)).getAnswer();
    }

    @Test
    void testCheckAnswer_WhenCurrentQAIsNull_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            boolean result = dialogLogic.checkAnswer("любой ответ");
            assertFalse(result);
        });
    }

    @Test
    void testGetHelp_ShouldReturnHelpString() {
        String help = dialogLogic.getHelp();

        assertNotNull(help);
        assertFalse(help.isEmpty());
        assertTrue(help.contains("ООП"));
        assertTrue(help.contains("справочка"));
    }

    @Test
    void testConstructor_ShouldInitializeRepository() {
        assertNotNull(dialogLogic);
    }
}
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

        IQuestion result = dialogLogic.getQuestion();

        assertNotNull(result);
        assertEquals(question, result);
        verify(questionRepository, times(1)).getRandomQuestion();
    }

    @Test
    void testProcessAnswer_WhenAnswerIsCorrect_ShouldReturnTrue() {
        String correctAnswer = "правильный ответ";
        when(question.getAnswer()).thenReturn(correctAnswer);
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        dialogLogic.getQuestion();

        boolean result = dialogLogic.processAnswer(correctAnswer);

        assertTrue(result);
        verify(question, times(1)).getAnswer();
    }

    @Test
    void testProcessAnswer_WhenAnswerIsIncorrect_ShouldReturnFalse() {
        String correctAnswer = "правильный ответ";
        String wrongAnswer = "неправильный ответ";
        when(question.getAnswer()).thenReturn(correctAnswer);
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        dialogLogic.getQuestion();

        boolean result = dialogLogic.processAnswer(wrongAnswer);

        assertFalse(result);
        verify(question, times(1)).getAnswer();
    }

    @Test
    void testProcessAnswer_WhenAnswerIsNull_ShouldHandleCorrectly() {
        String correctAnswer = "правильный ответ";
        when(question.getAnswer()).thenReturn(correctAnswer);
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        dialogLogic.getQuestion();

        boolean result = dialogLogic.processAnswer(null);

        assertFalse(result);
        verify(question, times(1)).getAnswer();
    }

    @Test
    void testProcessAnswer_WhenCurrentQAIsNull_ShouldNotThrowException() {
        assertDoesNotThrow(() -> {
            boolean result = dialogLogic.processAnswer("любой ответ");
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
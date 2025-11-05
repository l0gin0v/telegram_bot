package com.utils.tests;

import com.utils.interfaces.IQuestion;
import com.utils.interfaces.IQuestionRepository;
import com.utils.models.UserAnswerStatus;
import com.utils.services.DialogLogic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DialogLogicTest {
    
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
    void testGetQuestion() {
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        when(question.getQuestion()).thenReturn("Сколько будет 2+2?");
        
        String result = dialogLogic.getQuestion();
        
        assertNotNull(result);
        assertTrue(result.contains("Вопрос: Сколько будет 2+2?"));
        verify(questionRepository, times(1)).getRandomQuestion();
    }

    @Test
    void testNeedToStart() {
        String result = dialogLogic.needToStart();
        assertEquals("Для запуска бота введите /start", result);
    }

    @Test
    void testWelcomeWords() {
        String result = dialogLogic.welcomeWords();
        
        assertNotNull(result);
        assertTrue(result.contains("Добро пожаловать в бота!"));
        assertTrue(result.contains("/help"));
        assertTrue(result.contains("/quit"));
    }

    @Test
    void testProcessAnswer_CorrectAnswer() {
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        when(question.getAnswer()).thenReturn("4");
        dialogLogic.getQuestion();
        
        UserAnswerStatus result = dialogLogic.processAnswer("4");
        
        assertTrue(result.isCorrectAnswer);
        assertEquals("Правильно! Отличная работа!", result.message);
        assertFalse(result.isQuit);
    }

    @Test
    void testProcessAnswer_WrongAnswer() {
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        when(question.getAnswer()).thenReturn("4");
        dialogLogic.getQuestion();
        
        UserAnswerStatus result = dialogLogic.processAnswer("5");
        
        assertFalse(result.isCorrectAnswer);
        assertEquals("Неправильно. Попробуйте еще раз или введите /quit", result.message);
        assertFalse(result.isQuit);
    }

    @Test
    void testProcessAnswer_HelpCommand() {
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        dialogLogic.getQuestion();
        
        UserAnswerStatus result = dialogLogic.processAnswer("/help");
        
        assertFalse(result.isCorrectAnswer);
        assertTrue(result.message.contains("Это бот для курса ООП"));
        assertFalse(result.isQuit);
    }

    @Test
    void testProcessAnswer_QuitCommand() {
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        dialogLogic.getQuestion();
        
        UserAnswerStatus result = dialogLogic.processAnswer("/quit");
        
        assertFalse(result.isCorrectAnswer);
        assertEquals("До свидания! Возвращайтесь еще!", result.message);
        assertTrue(result.isQuit);
    }

    @Test
    void testProcessAnswer_WithoutCurrentQuestion() {
        assertThrows(NullPointerException.class, () -> {
            dialogLogic.processAnswer("ответ");
        });
    }

    @Test
    void testProcessAnswer_EmptyAnswer() {
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        when(question.getAnswer()).thenReturn("4");
        dialogLogic.getQuestion();
        
        UserAnswerStatus result = dialogLogic.processAnswer("");
        
        assertFalse(result.isCorrectAnswer);
        assertEquals("Неправильно. Попробуйте еще раз или введите /quit", result.message);
    }
}
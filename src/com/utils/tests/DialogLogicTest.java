package src.com.utils.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import src.com.utils.interfaces.IQuestion;
import src.com.utils.interfaces.IQuestionRepository;
import src.com.utils.services.DialogLogic;

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
        // Arrange
        when(questionRepository.getRandomQuestion()).thenReturn(question);

        // Act
        IQuestion result = dialogLogic.getQA();

        // Assert
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
        // Arrange
        String correctAnswer = "правильный ответ";
        when(question.getAnswer()).thenReturn(correctAnswer);
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        dialogLogic.getQA(); // Устанавливаем currentQA

        // Act
        boolean result = dialogLogic.checkAnswer(correctAnswer);

        // Assert
        assertTrue(result);
        verify(question, times(1)).getAnswer();
    }

    @Test
    void testCheckAnswer_WhenAnswerIsIncorrect_ShouldReturnFalse() {
        // Arrange
        String correctAnswer = "правильный ответ";
        String wrongAnswer = "неправильный ответ";
        when(question.getAnswer()).thenReturn(correctAnswer);
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        dialogLogic.getQA(); // Устанавливаем currentQA

        // Act
        boolean result = dialogLogic.checkAnswer(wrongAnswer);

        // Assert
        assertFalse(result);
        verify(question, times(1)).getAnswer();
    }

    @Test
    void testCheckAnswer_WhenAnswerIsNull_ShouldHandleCorrectly() {
        // Arrange
        String correctAnswer = "правильный ответ";
        when(question.getAnswer()).thenReturn(correctAnswer);
        when(questionRepository.getRandomQuestion()).thenReturn(question);
        dialogLogic.getQA(); // Устанавливаем currentQA

        // Act
        boolean result = dialogLogic.checkAnswer(null);

        // Assert
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
        // Act
        String help = dialogLogic.getHelp();

        // Assert
        assertNotNull(help);
        assertFalse(help.isEmpty());
        assertTrue(help.contains("ООП"));
        assertTrue(help.contains("справочка"));
    }

    @Test
    void testConstructor_ShouldInitializeRepository() {
        // Act & Assert
        assertNotNull(dialogLogic);
    }
}
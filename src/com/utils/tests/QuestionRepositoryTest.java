package src.com.utils.tests;

import src.com.utils.models.Question;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import src.com.utils.services.QuestionRepository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class QuestionRepositoryTest {
    private QuestionRepository questionRepository;

    @BeforeEach
    void setUp() {
        questionRepository = new QuestionRepository();
    }

    @Test
    void testGetRandomQuestion_ReturnsNotNull() {
        // Act
        Question result = questionRepository.getRandomQuestion();

        // Assert
        assertNotNull(result, "Метод должен возвращать вопрос, а не null");
    }

    @Test
    void testGetRandomQuestion_ReturnsValidQuestionObject() {
        // Act
        Question result = questionRepository.getRandomQuestion();

        // Assert
        assertNotNull(result.getQuestion(), "Текст вопроса не должен быть null");
        assertNotNull(result.getAnswer(), "Ответ не должен быть null");
        assertNotNull(result.getId(), "ID не должен быть null");

        assertFalse(result.getQuestion().isEmpty(), "Текст вопроса не должен быть пустым");
        assertFalse(result.getAnswer().isEmpty(), "Ответ не должен быть пустым");
    }

    @Test
    void testGetRandomQuestion_ReturnsOneOfPredefinedQuestions() {
        // Arrange
        List<String> expectedQuestions = Arrays.asList("7 + 7", "1 + 2", "50 + 5", "14 + 7");
        List<String> expectedAnswers = Arrays.asList("14", "3", "55", "21");

        // Act
        Question result = questionRepository.getRandomQuestion();

        // Assert
        assertTrue(expectedQuestions.contains(result.getQuestion()),
                "Вопрос должен быть одним из предопределенных: " + result.getQuestion());
        assertTrue(expectedAnswers.contains(result.getAnswer()),
                "Ответ должен быть одним из предопределенных: " + result.getAnswer());
    }


    @Test
    void testQuestionRepository_Initialization() {
        assertDoesNotThrow(() -> {
            QuestionRepository repo = new QuestionRepository();
        }, "Конструктор не должен выбрасывать исключения");
    }

}
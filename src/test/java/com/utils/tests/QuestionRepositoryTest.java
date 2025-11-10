package com.utils.tests;

import com.utils.models.Question;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import com.utils.services.QuestionRepository;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        Assertions.assertNotNull(result, "Метод должен возвращать вопрос, а не null");
    }

    @Test
    void testGetRandomQuestion_ReturnsValidQuestionObject() {
        // Act
        Question result = questionRepository.getRandomQuestion();

        // Assert
        Assertions.assertNotNull(result.getQuestion(), "Текст вопроса не должен быть null");
        Assertions.assertNotNull(result.getAnswer(), "Ответ не должен быть null");
        Assertions.assertNotNull(result.getId(), "ID не должен быть null");

        Assertions.assertFalse(result.getQuestion().isEmpty(), "Текст вопроса не должен быть пустым");
        Assertions.assertFalse(result.getAnswer().isEmpty(), "Ответ не должен быть пустым");
    }

    @Test
    void testGetRandomQuestion_ReturnsOneOfPredefinedQuestions() {
        // Arrange
        List<String> expectedQuestions = Arrays.asList("7 + 7", "1 + 2", "50 + 5", "14 + 7");
        List<String> expectedAnswers = Arrays.asList("14", "3", "55", "21");

        // Act
        Question result = questionRepository.getRandomQuestion();

        // Assert
        Assertions.assertTrue(expectedQuestions.contains(result.getQuestion()),
                "Вопрос должен быть одним из предопределенных: " + result.getQuestion());
        Assertions.assertTrue(expectedAnswers.contains(result.getAnswer()),
                "Ответ должен быть одним из предопределенных: " + result.getAnswer());
    }


    @Test
    void getRandomQuestion_ShouldReturnMockedQuestion() {
        // Arrange
        QuestionRepository mockRepository = mock(QuestionRepository.class);
        Question expected = new Question(1, "7 + 7", "14");

        when(mockRepository.getRandomQuestion()).thenReturn(expected);

        // Act
        Question actual = mockRepository.getRandomQuestion();

        // Assert
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testQuestionRepository_Initialization() {
        Assertions.assertDoesNotThrow(() -> {
            QuestionRepository repo = new QuestionRepository();
        }, "Конструктор не должен выбрасывать исключения");
    }

}
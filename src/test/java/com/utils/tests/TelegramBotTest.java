package com.utils.tests;

import com.utils.interfaces.IDialogLogic;
import com.utils.services.TelegramBot;
import com.utils.models.UserAnswerStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramBotTest {

    @Mock
    private Supplier<IDialogLogic> dialogLogicFactory;

    @Mock
    private IDialogLogic dialogLogic;

    private TelegramBot telegramBot;
    private final String BOT_USERNAME = "test_bot";
    private final String BOT_TOKEN = "test_token";

    @BeforeEach
    void setUp() {
        telegramBot = new TelegramBot(dialogLogicFactory, BOT_USERNAME, BOT_TOKEN);
    }

    @Test
    void testBotUsernameAndToken() {
        assertEquals(BOT_USERNAME, telegramBot.getBotUsername());
        assertEquals(BOT_TOKEN, telegramBot.getBotToken());
    }

    @Test
    void testStartCommandCreatesNewSession() throws TelegramApiException {
        // Arrange
        when(dialogLogicFactory.get()).thenReturn(dialogLogic);
        when(dialogLogic.welcomeWords()).thenReturn("Добро пожаловать!");
        when(dialogLogic.getQuestion()).thenReturn("Первый вопрос?");

        Update update = createTextUpdate(123L, "/start");

        // Act
        telegramBot.onUpdateReceived(update);

        // Assert
        verify(dialogLogicFactory).get();
        verify(dialogLogic).welcomeWords();
        verify(dialogLogic).getQuestion();
    }

    @Test
    void testMultipleUsersSessions() throws TelegramApiException {
        // Arrange
        when(dialogLogicFactory.get()).thenReturn(dialogLogic);
        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question?");

        // Act
        Update user1Update = createTextUpdate(123L, "/start");
        Update user2Update = createTextUpdate(456L, "/start");

        telegramBot.onUpdateReceived(user1Update);
        telegramBot.onUpdateReceived(user2Update);

        // Assert
        verify(dialogLogicFactory, times(2)).get();
        verify(dialogLogic, times(2)).welcomeWords();
        verify(dialogLogic, times(2)).getQuestion();
    }

    @Test
    void testUserAnswerProcessing_CorrectAnswer() throws TelegramApiException {
        // Arrange
        when(dialogLogicFactory.get()).thenReturn(dialogLogic);
        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question?");

        UserAnswerStatus correctAnswerStatus = new UserAnswerStatus(true, "right", false);

        when(dialogLogic.processAnswer("правильный ответ")).thenReturn(correctAnswerStatus);

        // Start session first
        Update startUpdate = createTextUpdate(123L, "/start");
        telegramBot.onUpdateReceived(startUpdate);

        // Then send correct answer
        Update answerUpdate = createTextUpdate(123L, "правильный ответ");
        telegramBot.onUpdateReceived(answerUpdate);

        // Assert
        verify(dialogLogic).processAnswer("правильный ответ");
        verify(dialogLogic, times(2)).getQuestion();
    }

    @Test
    void testUserAnswerProcessing_WrongAnswer() throws TelegramApiException {
        // Arrange
        when(dialogLogicFactory.get()).thenReturn(dialogLogic);
        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question?");

        UserAnswerStatus wrongAnswerStatus = new UserAnswerStatus(false,  "wrong", false);

        when(dialogLogic.processAnswer("неправильный ответ")).thenReturn(wrongAnswerStatus);

        // Start session
        Update startUpdate = createTextUpdate(123L, "/start");
        telegramBot.onUpdateReceived(startUpdate);

        // Send wrong answer
        Update answerUpdate = createTextUpdate(123L, "неправильный ответ");
        telegramBot.onUpdateReceived(answerUpdate);

        // Assert
        verify(dialogLogic).processAnswer("неправильный ответ");
        verify(dialogLogic, times(1)).getQuestion();
    }

    @Test
    void testUserAnswerProcessing_QuitCommand() throws TelegramApiException {
        // Arrange
        when(dialogLogicFactory.get()).thenReturn(dialogLogic);
        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question?");
        when(dialogLogic.needToStart()).thenReturn("Начните с /start");

        UserAnswerStatus quitAnswerStatus = new UserAnswerStatus(false, "quit", true);

        when(dialogLogic.processAnswer("quit")).thenReturn(quitAnswerStatus);

        // Start session
        Update startUpdate = createTextUpdate(123L, "/start");
        telegramBot.onUpdateReceived(startUpdate);

        // Send quit command
        Update quitUpdate = createTextUpdate(123L, "quit");
        telegramBot.onUpdateReceived(quitUpdate);

        // Assert
        verify(dialogLogic).processAnswer("quit");

        // Try to send another message after quit
        Update anotherUpdate = createTextUpdate(123L, "еще сообщение");
        telegramBot.onUpdateReceived(anotherUpdate);

        // Should receive needToStart message
        verify(dialogLogic).needToStart();
    }

    @Test
    void testRestartSessionWithStartCommand() throws TelegramApiException {
        // Arrange
        when(dialogLogicFactory.get()).thenReturn(dialogLogic);
        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question?");

        // Start first session
        Update startUpdate1 = createTextUpdate(123L, "/start");
        telegramBot.onUpdateReceived(startUpdate1);

        // Restart session with /start command
        Update startUpdate2 = createTextUpdate(123L, "/start");
        telegramBot.onUpdateReceived(startUpdate2);

        // Assert
        verify(dialogLogicFactory, times(2)).get();
        verify(dialogLogic, times(2)).welcomeWords();
        verify(dialogLogic, times(2)).getQuestion();
    }

    @Test
    void testSendMessageFormat() throws TelegramApiException {
        // Arrange
        TelegramBot spyBot = spy(telegramBot);

        Message mockMessage = mock(Message.class);
        doReturn(mockMessage).when(spyBot).execute(any(SendMessage.class));

        when(dialogLogicFactory.get()).thenReturn(dialogLogic);
        when(dialogLogic.welcomeWords()).thenReturn("Test welcome");
        when(dialogLogic.getQuestion()).thenReturn("First question?");

        // Act
        Update update = createTextUpdate(123L, "/start");
        spyBot.onUpdateReceived(update);

        // Assert
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(spyBot, atLeastOnce()).execute(messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getAllValues().get(0);
        assertEquals("123", sentMessage.getChatId());
        assertEquals("Test welcome", sentMessage.getText());
    }

    @Test
    void testNoSession_MessageWithoutStart() throws TelegramApiException {
        // Arrange
        when(dialogLogicFactory.get()).thenReturn(dialogLogic);
        when(dialogLogic.needToStart()).thenReturn("Пожалуйста, начните с /start");

        // Act
        Update update = createTextUpdate(123L, "просто сообщение");
        telegramBot.onUpdateReceived(update);

        // Assert
        verify(dialogLogic).needToStart();
    }

    @Test
    void testMessageWhenNotWaitingForAnswer() throws TelegramApiException {
        // Arrange
        TelegramBot spyBot = spy(telegramBot);

        Message mockMessage = mock(Message.class);

        doReturn(mockMessage).when(spyBot).execute(any(SendMessage.class));

        when(dialogLogicFactory.get()).thenReturn(dialogLogic);
        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question?");

        UserAnswerStatus answerStatus = new UserAnswerStatus(true, "right", false);
        when(dialogLogic.processAnswer("ответ")).thenReturn(answerStatus);

        // Start session and answer first question
        Update startUpdate = createTextUpdate(123L, "/start");
        spyBot.onUpdateReceived(startUpdate);

        Update answerUpdate = createTextUpdate(123L, "ответ");
        spyBot.onUpdateReceived(answerUpdate);

        // Send another message immediately (when not waiting for answer)
        UserAnswerStatus extraMessageStatus = new UserAnswerStatus(false, "Не сейчас", false);
        when(dialogLogic.processAnswer("лишнее сообщение")).thenReturn(extraMessageStatus);

        Update extraUpdate = createTextUpdate(123L, "лишнее сообщение");
        spyBot.onUpdateReceived(extraUpdate);

        // Assert
        verify(dialogLogic, times(1)).processAnswer("ответ");
        verify(dialogLogic, times(1)).processAnswer("лишнее сообщение");
    }

    @Test
    void testTelegramApiExceptionHandling() throws TelegramApiException {
        // Arrange
        TelegramBot spyBot = spy(telegramBot);
        doThrow(new TelegramApiException("API error")).when(spyBot).execute(any(SendMessage.class));

        when(dialogLogicFactory.get()).thenReturn(dialogLogic);
        when(dialogLogic.welcomeWords()).thenReturn("Welcome");
        when(dialogLogic.getQuestion()).thenReturn("Question?");

        // Act
        Update update = createTextUpdate(123L, "/start");
        spyBot.onUpdateReceived(update);

        // Assert
        verify(spyBot, atLeastOnce()).execute(any(SendMessage.class));
    }

    @Test
    void testUpdateWithoutText() {
        // Arrange
        Update update = new Update();
        Message message = new Message();
        // message has no text
        update.setMessage(message);

        // Act
        telegramBot.onUpdateReceived(update);

        // Assert
        verifyNoInteractions(dialogLogicFactory);
    }

    @Test
    void testUpdateWithoutMessage() {
        // Arrange
        Update update = new Update();

        // Act
        telegramBot.onUpdateReceived(update);

        // Assert
        verifyNoInteractions(dialogLogicFactory);
    }

    private Update createTextUpdate(Long chatId, String text) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();

        chat.setId(chatId);
        message.setChat(chat);
        message.setText(text);
        update.setMessage(message);

        return update;
    }
}

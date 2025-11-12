package com.utils.services;

import com.utils.interfaces.IDialogLogic;
import com.utils.models.UserAnswerStatus;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class TelegramBot extends TelegramLongPollingBot {
    private final IDialogLogic dialogLogic;
    private final Map<Long, UserSession> userSessions;
    private final String botUsername;
    private final String botToken;

    public TelegramBot(IDialogLogic dialogLogic, String botUsername, String botToken) {
        this.dialogLogic = dialogLogic;
        this.userSessions = new HashMap<>();
        this.botUsername = botUsername;
        this.botToken = botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText().trim();

            handleUserMessage(chatId, messageText);
        }
    }

    private void handleUserMessage(Long chatId, String messageText) {
        UserSession session = userSessions.get(chatId);

        if (session == null || "/start".equals(messageText)) {
            startNewSession(chatId);
            return;
        }

        if (!session.isRunning()) {
            sendMessage(chatId, dialogLogic.needToStart());
            return;
        }

        handleAnswer(chatId, messageText, session);
    }

    private void startNewSession(Long chatId) {
        UserSession newSession = new UserSession();
        newSession.setRunning(true);
        userSessions.put(chatId, newSession);

        sendMessage(chatId, dialogLogic.welcomeWords());
        askNextQuestion(chatId, newSession);
    }

    private void handleAnswer(Long chatId, String userInput, UserSession session) {
        if (!session.isWaitingForAnswer()) {
            return;
        }

        UserAnswerStatus userAnswerStatus = dialogLogic.processAnswer(userInput);
        sendMessage(chatId, userAnswerStatus.message);

        session.setWaitingForAnswer(false);

        if (userAnswerStatus.isQuit) {
            session.setRunning(false);
            sendMessage(chatId, "Сессия завершена. Напишите /start чтобы начать заново.");
        } else if (userAnswerStatus.isCorrectAnswer) {
            askNextQuestion(chatId, session);
        } else {
            session.setWaitingForAnswer(true);
        }
    }

    private void askNextQuestion(Long chatId, UserSession session) {
        if (!session.isRunning()) {
            return;
        }

        String question = dialogLogic.getQuestion();
        sendMessage(chatId, question);
        session.setWaitingForAnswer(true);
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private static class UserSession {
        private boolean isRunning = false;
        private boolean isWaitingForAnswer = false;

        public boolean isRunning() { return isRunning; }
        public void setRunning(boolean running) { isRunning = running; }
        public boolean isWaitingForAnswer() { return isWaitingForAnswer; }
        public void setWaitingForAnswer(boolean waiting) { isWaitingForAnswer = waiting; }
    }
}
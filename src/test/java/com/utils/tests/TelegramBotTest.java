package com.utils.tests;

import com.utils.services.TelegramBot;
import com.utils.services.WeatherAPI;
import com.utils.services.Geocoding;
import com.utils.models.Coordinates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelegramBotTest {

    @Mock
    private WeatherAPI mockWeatherAPI;

    @Mock
    private Geocoding mockGeocoding;

    @Mock
    private Message mockTelegramMessage;

    private TelegramBot telegramBot;
    private final long TEST_CHAT_ID = 12345L;
    private final String BOT_USERNAME = "test_bot";
    private final String BOT_TOKEN = "test_token";

    @BeforeEach
    void setUp() throws Exception {
        telegramBot = new TelegramBot(BOT_USERNAME, BOT_TOKEN);
        setPrivateField(telegramBot, "weatherAPI", mockWeatherAPI);
        setPrivateField(telegramBot, "geocodingService", mockGeocoding);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(Object target, String fieldName) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
    }

    private Object getUserStateValue(String stateName) throws Exception {
        for (Class<?> declaredClass : TelegramBot.class.getDeclaredClasses()) {
            if (declaredClass.getSimpleName().equals("UserState")) {
                Field field = declaredClass.getDeclaredField(stateName);
                field.setAccessible(true);
                return field.get(null);
            }
        }
        throw new IllegalArgumentException("UserState." + stateName + " not found");
    }

    private Update createTextUpdate(long chatId, String text) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();

        chat.setId(chatId);
        message.setChat(chat);
        message.setText(text);
        update.setMessage(message);

        return update;
    }

    @Test
    void getBotUsername_ShouldReturnConfiguredUsername() {
        assertEquals(BOT_USERNAME, telegramBot.getBotUsername());
    }

    @Test
    void getBotToken_ShouldReturnConfiguredToken() {
        assertEquals(BOT_TOKEN, telegramBot.getBotToken());
    }

    @Test
    void startCommand_ShouldInitializeUserSession() throws Exception {
        Update update = createTextUpdate(TEST_CHAT_ID, "/start");

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        botSpy.onUpdateReceived(update);

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        assertTrue(userSessions.get(TEST_CHAT_ID));
    }

    @Test
    void helpCommandWithActiveSession_ShouldProcessHelp() throws Exception {
        Update update = createTextUpdate(TEST_CHAT_ID, "/help");

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        userSessions.put(TEST_CHAT_ID, true);

        botSpy.onUpdateReceived(update);

        verify(botSpy, atLeastOnce()).execute(any(SendMessage.class));
    }

    @Test
    void quitCommand_ShouldEndUserSession() throws Exception {
        Update update = createTextUpdate(TEST_CHAT_ID, "/quit");

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        userSessions.put(TEST_CHAT_ID, true);

        botSpy.onUpdateReceived(update);

        assertFalse(userSessions.get(TEST_CHAT_ID));
    }

    @Test
    void weatherRequestWithCity_ShouldCallWeatherAPI() throws Exception {
        Update update = createTextUpdate(TEST_CHAT_ID, "🌤 Сегодня");

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        userSessions.put(TEST_CHAT_ID, true);

        Map<Long, String> userCities = getPrivateField(botSpy, "userCities");
        userCities.put(TEST_CHAT_ID, "Москва");

        when(mockWeatherAPI.getFormattedWeatherByCity("Москва", 1))
                .thenReturn("Погода в Москве: солнечно, +20°C");

        botSpy.onUpdateReceived(update);

        verify(mockWeatherAPI, times(1)).getFormattedWeatherByCity("Москва", 1);
    }

    @Test
    void weatherRequestWithoutCity_ShouldNotCallWeatherAPI() throws Exception {
        Update update = createTextUpdate(TEST_CHAT_ID, "🌤 Сегодня");

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        userSessions.put(TEST_CHAT_ID, true);

        botSpy.onUpdateReceived(update);

        verify(mockWeatherAPI, never()).getFormattedWeatherByCity(anyString(), anyInt());
    }

    @Test
    void changeCityCommand_ShouldSetWaitingForCityState() throws Exception {
        Update update = createTextUpdate(TEST_CHAT_ID, "📍 Сменить город");

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        userSessions.put(TEST_CHAT_ID, true);

        botSpy.onUpdateReceived(update);

        Map<Long, Object> userStates = getPrivateField(botSpy, "userStates");
        Object waitingState = getUserStateValue("WAITING_FOR_CITY");
        assertEquals(waitingState, userStates.get(TEST_CHAT_ID));
    }

    @Test
    void validCityInput_ShouldSetCityAndReturnToDefaultState() throws Exception {
        Update update = createTextUpdate(TEST_CHAT_ID, "Москва");
        Coordinates coordinates = new Coordinates(55.7558, 37.6173, "Москва, Россия");

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        userSessions.put(TEST_CHAT_ID, true);

        when(mockGeocoding.getCoordinates("Москва")).thenReturn(coordinates);

        Map<Long, Object> userStates = getPrivateField(botSpy, "userStates");
        Object waitingState = getUserStateValue("WAITING_FOR_CITY");
        userStates.put(TEST_CHAT_ID, waitingState);

        botSpy.onUpdateReceived(update);

        Map<Long, String> userCities = getPrivateField(botSpy, "userCities");
        assertEquals("Москва", userCities.get(TEST_CHAT_ID));
    }

    @Test
    void invalidCityInput_ShouldKeepWaitingState() throws Exception {
        Update update = createTextUpdate(TEST_CHAT_ID, "НесуществующийГород");

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        userSessions.put(TEST_CHAT_ID, true);

        when(mockGeocoding.getCoordinates("НесуществующийГород"))
                .thenThrow(new RuntimeException("Город не найден"));

        Map<Long, Object> userStates = getPrivateField(botSpy, "userStates");
        Object waitingState = getUserStateValue("WAITING_FOR_CITY");
        userStates.put(TEST_CHAT_ID, waitingState);

        botSpy.onUpdateReceived(update);

        assertEquals(waitingState, userStates.get(TEST_CHAT_ID));
    }

    @Test
    void inactiveSession_ShouldSendInactiveMessage() throws Exception {
        Update update = createTextUpdate(TEST_CHAT_ID, "🌤 Сегодня");

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        userSessions.put(TEST_CHAT_ID, false);

        botSpy.onUpdateReceived(update);

        verify(botSpy, atLeastOnce()).execute(any(SendMessage.class));
    }

    @Test
    void multipleUsers_ShouldHaveIndependentSessions() throws Exception {
        long user1ChatId = 11111L;
        long user2ChatId = 22222L;

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        Update user1Start = createTextUpdate(user1ChatId, "/start");
        Update user2Start = createTextUpdate(user2ChatId, "/start");

        botSpy.onUpdateReceived(user1Start);
        botSpy.onUpdateReceived(user2Start);

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        assertTrue(userSessions.get(user1ChatId));
        assertTrue(userSessions.get(user2ChatId));
    }

    @Test
    void popularCitiesCommand_ShouldShowCitiesKeyboard() throws Exception {
        Update update = createTextUpdate(TEST_CHAT_ID, "🏙 Популярные города");

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        userSessions.put(TEST_CHAT_ID, true);

        botSpy.onUpdateReceived(update);

        Map<Long, Object> userStates = getPrivateField(botSpy, "userStates");
        Object waitingState = getUserStateValue("WAITING_FOR_CITY");
        assertEquals(waitingState, userStates.get(TEST_CHAT_ID));
    }

    @Test
    void cancelCommandInWaitingState_ShouldReturnToDefaultState() throws Exception {
        Update update = createTextUpdate(TEST_CHAT_ID, "↩️ Отмена");

        TelegramBot botSpy = spy(telegramBot);
        doReturn(mockTelegramMessage).when(botSpy).execute(any(SendMessage.class));

        Map<Long, Boolean> userSessions = getPrivateField(botSpy, "userSessions");
        userSessions.put(TEST_CHAT_ID, true);

        Map<Long, Object> userStates = getPrivateField(botSpy, "userStates");
        Object waitingState = getUserStateValue("WAITING_FOR_CITY");
        userStates.put(TEST_CHAT_ID, waitingState);

        botSpy.onUpdateReceived(update);

        Object defaultState = getUserStateValue("DEFAULT");
        assertEquals(defaultState, userStates.get(TEST_CHAT_ID));
    }

    @Test
    void updateWithoutMessage_ShouldBeIgnored() {
        Update update = new Update();

        telegramBot.onUpdateReceived(update);

        verifyNoInteractions(mockWeatherAPI);
        verifyNoInteractions(mockGeocoding);
    }

    @Test
    void updateWithoutText_ShouldBeIgnored() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();

        chat.setId(TEST_CHAT_ID);
        message.setChat(chat);
        update.setMessage(message);

        telegramBot.onUpdateReceived(update);

        verifyNoInteractions(mockWeatherAPI);
        verifyNoInteractions(mockGeocoding);
    }
}
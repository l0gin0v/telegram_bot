package com.utils.tests;

import com.utils.services.KeyboardFactory;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeyboardFactoryTest {

    @Test
    void createMainWeatherKeyboard_ShouldCreateCorrectStructure() {
        ReplyKeyboardMarkup keyboard = KeyboardFactory.createMainWeatherKeyboard();

        assertTrue(keyboard.getResizeKeyboard());
        assertFalse(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> keyboardRows = keyboard.getKeyboard();
        assertEquals(4, keyboardRows.size());

        KeyboardRow row1 = keyboardRows.get(0);
        assertEquals("🌤 Сегодня", row1.get(0).getText());
        assertEquals("📅 Завтра", row1.get(1).getText());

        KeyboardRow row2 = keyboardRows.get(1);
        assertEquals("📆 3 дня", row2.get(0).getText());
        assertEquals("🗓 Неделя", row2.get(1).getText());

        KeyboardRow row3 = keyboardRows.get(2);
        assertEquals("📍 Сменить город", row3.get(0).getText());
        assertEquals("🏙 Популярные города", row3.get(1).getText());

        KeyboardRow row4 = keyboardRows.get(3);
        assertEquals("/help", row4.get(0).getText());
        assertEquals("/quit", row4.get(1).getText());
    }

    @Test
    void createCitiesKeyboard_ShouldCreateCorrectCities() {
        ReplyKeyboardMarkup keyboard = KeyboardFactory.createCitiesKeyboard();

        assertTrue(keyboard.getResizeKeyboard());
        assertTrue(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> keyboardRows = keyboard.getKeyboard();
        assertEquals(5, keyboardRows.size());

        KeyboardRow row1 = keyboardRows.get(0);
        assertEquals("Москва", row1.get(0).getText());
        assertEquals("Санкт-Петербург", row1.get(1).getText());

        KeyboardRow row2 = keyboardRows.get(1);
        assertEquals("Новосибирск", row2.get(0).getText());
        assertEquals("Екатеринбург", row2.get(1).getText());

        KeyboardRow row5 = keyboardRows.get(4);
        assertEquals("↩️ Назад", row5.get(0).getText());
    }

    @Test
    void createCancelKeyboard_ShouldCreateSingleButton() {
        ReplyKeyboardMarkup keyboard = KeyboardFactory.createCancelKeyboard();

        assertTrue(keyboard.getResizeKeyboard());
        assertTrue(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> keyboardRows = keyboard.getKeyboard();
        assertEquals(1, keyboardRows.size());

        KeyboardRow row = keyboardRows.get(0);
        assertEquals(1, row.size());
        assertEquals("↩️ Отмена", row.get(0).getText());
    }

    @Test
    void createInlineWeatherKeyboard_ShouldCreateCorrectInlineButtons() {
        InlineKeyboardMarkup keyboard = KeyboardFactory.createInlineWeatherKeyboard();

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertEquals(2, rows.size());

        List<InlineKeyboardButton> row1 = rows.get(0);
        assertEquals("🌤 Сегодня", row1.get(0).getText());
        assertEquals("weather_today", row1.get(0).getCallbackData());
        assertEquals("📅 Завтра", row1.get(1).getText());
        assertEquals("weather_tomorrow", row1.get(1).getCallbackData());

        List<InlineKeyboardButton> row2 = rows.get(1);
        assertEquals("📆 3 дня", row2.get(0).getText());
        assertEquals("weather_3days", row2.get(0).getCallbackData());
        assertEquals("🗓 Неделя", row2.get(1).getText());
        assertEquals("weather_week", row2.get(1).getCallbackData());
    }

    @Test
    void createInlineCitiesKeyboard_ShouldCreateCorrectCityButtons() {
        InlineKeyboardMarkup keyboard = KeyboardFactory.createInlineCitiesKeyboard();

        List<List<InlineKeyboardButton>> rows = keyboard.getKeyboard();
        assertEquals(3, rows.size());

        List<InlineKeyboardButton> row1 = rows.get(0);
        assertEquals("Москва", row1.get(0).getText());
        assertEquals("city_moscow", row1.get(0).getCallbackData());
        assertEquals("СПб", row1.get(1).getText());
        assertEquals("city_spb", row1.get(1).getCallbackData());

        List<InlineKeyboardButton> row3 = rows.get(2);
        assertEquals("Казань", row3.get(0).getText());
        assertEquals("city_kazan", row3.get(0).getCallbackData());
        assertEquals("Сочи", row3.get(1).getText());
        assertEquals("city_sochi", row3.get(1).getCallbackData());
    }

    @Test
    void createConfirmationKeyboard_ShouldCreateYesNoButtons() {
        ReplyKeyboardMarkup keyboard = KeyboardFactory.createConfirmationKeyboard();

        assertTrue(keyboard.getResizeKeyboard());
        assertTrue(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> keyboardRows = keyboard.getKeyboard();
        assertEquals(1, keyboardRows.size());

        KeyboardRow row = keyboardRows.get(0);
        assertEquals(2, row.size());
        assertEquals("✅ Да", row.get(0).getText());
        assertEquals("❌ Нет", row.get(1).getText());
    }

    @Test
    void createStartKeyboard_ShouldCreateStartAndWeatherButtons() {
        ReplyKeyboardMarkup keyboard = KeyboardFactory.createStartKeyboard();

        assertTrue(keyboard.getResizeKeyboard());
        assertFalse(keyboard.getOneTimeKeyboard());

        List<KeyboardRow> keyboardRows = keyboard.getKeyboard();
        assertEquals(1, keyboardRows.size());

        KeyboardRow row = keyboardRows.get(0);
        assertEquals(2, row.size());
        assertEquals("/start", row.get(0).getText());
        assertEquals("🌤 Погода", row.get(1).getText());
    }
}
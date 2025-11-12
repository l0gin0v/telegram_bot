import com.utils.services.TelegramBot;
import com.utils.services.DialogLogic;
import com.utils.services.QuestionRepository;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;


public class Main {
    public static void main(String[] args) {
        QuestionRepository questionRepository = new QuestionRepository();
        DialogLogic dialogLogic = new DialogLogic(questionRepository);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            String botUsername = "t.me/LoginovShagapovBot";
            String botToken = "7761722257:AAH9jQrlRpRlA9NkXU4m1z0mbVMGPjOBZkA";

            botsApi.registerBot(new TelegramBot(dialogLogic, botUsername, botToken));

            System.out.println("Telegram bot started successfully!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
import com.utils.services.TelegramBot;
import com.utils.services.DialogLogic;
import com.utils.services.QuestionRepository;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        QuestionRepository questionRepository = new QuestionRepository();
        var dialogLogicFactory = (java.util.function.Supplier<com.utils.interfaces.IDialogLogic>) () ->
                new DialogLogic(questionRepository);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            String botUsername = "t.me/LoginovShagapovBot";
            String botToken = "7761722257:AAH9jQrlRpRlA9NkXU4m1z0mbVMGPjOBZkA";
            botsApi.registerBot(new TelegramBot(dialogLogicFactory, botUsername, botToken));

            System.out.println("Telegram bot started successfully!");
            System.out.println("Multiple users can now use the bot simultaneously!");

        } catch (TelegramApiException e) {
            e.printStackTrace();
            System.err.println("Failed to start Telegram bot: " + e.getMessage());
        }
    }
}
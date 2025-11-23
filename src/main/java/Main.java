import com.utils.services.TelegramBot;
import com.utils.services.Console;
import com.utils.services.DialogLogic;
import com.utils.services.QuestionRepository;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }

        try {
            int mode = Integer.parseInt(args[0]);
            runMode(mode);
        } catch (NumberFormatException e) {
            System.err.println("Ошибка: Аргумент должен быть числом (1, 2 или 3)");
            printUsage();
        } catch (Exception e) {
            System.err.println("Ошибка при запуске: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runMode(int mode) {
        switch (mode) {
            case 1:
                startTelegramBot();
                break;
            case 2:
                startConsoleBot();
                break;
            case 3:
                startBothBots();
                break;
            default:
                System.err.println("Неизвестный режим: " + mode);
                printUsage();
                break;
        }
    }

    private static void startTelegramBot() {
        System.out.println("=== ЗАПУСК TELEGRAM БОТА ===");

        String botUsername = System.getenv("TELEGRAM_BOT_USERNAME");
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");

        if (botUsername == null || botToken == null) {
            System.err.println("Ошибка: Не установлены переменные окружения!");
            System.err.println("Установите переменные:");
            System.err.println("TELEGRAM_BOT_USERNAME - имя бота");
            System.err.println("TELEGRAM_BOT_TOKEN - токен бота");
            return;
        }

        QuestionRepository questionRepository = new QuestionRepository();

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new TelegramBot(
                    () -> new DialogLogic(questionRepository),
                    botUsername,
                    botToken
            ));

            System.out.println("Telegram bot started successfully!");
            System.out.println("Bot username: " + botUsername);
            System.out.println("Бот запущен и ожидает сообщений...");

        } catch (TelegramApiException e) {
            System.err.println("Failed to start Telegram bot: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void startConsoleBot() {
        System.out.println("=== ЗАПУСК КОНСОЛЬНОГО БОТА ===");

        QuestionRepository questionRepository = new QuestionRepository();
        DialogLogic dialogLogic = new DialogLogic(questionRepository);
        Console console = new Console(dialogLogic);

        console.runBot();
    }

    private static void startBothBots() {
        System.out.println("=== ЗАПУСК ОБОИХ БОТОВ ===");

        QuestionRepository questionRepository = new QuestionRepository();

        Thread telegramThread = new Thread(() -> {
            startTelegramBot();
        });
        telegramThread.setDaemon(true);
        telegramThread.start();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        startConsoleBot();

        System.out.println("Оба бота запущены. Консольный бот работает в основном потоке.");
    }

    private static void printUsage() {
        System.out.println("Использование: java Main <режим>");
        System.out.println("Режимы:");
        System.out.println("  1 - Запуск только Telegram бота");
        System.out.println("  2 - Запуск только консольного бота");
        System.out.println("  3 - Запуск обоих ботов");
        System.out.println();
        System.out.println("Примеры:");
        System.out.println("  java Main 1  - запуск Telegram бота");
        System.out.println("  java Main 2  - запуск консольной версии");
        System.out.println("  java Main 3  - запуск обеих версий одновременно");
    }
}
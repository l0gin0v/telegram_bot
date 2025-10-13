import com.utils.services.Console;
import com.utils.services.DialogLogic;
import com.utils.services.QuestionRepository;

public class Main {
	public static void main(String[] args) {
		QuestionRepository questionRepository = new QuestionRepository();
		DialogLogic dialogLogic = new DialogLogic(questionRepository);
		Console console = new Console(dialogLogic);

		console.start();
		console.runQuizCycle();
		console.stop();
	}
}

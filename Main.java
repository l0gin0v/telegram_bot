import src.com.utils.interfaces.IQuestion;
import src.com.utils.interfaces.IQuestionRepository;
import src.com.utils.interfaces.IDialogLogic;
import src.com.utils.interfaces.IConsole;
import src.com.utils.services.DialogLogic;

public class Main {
	public static void main(String[] args) {
		IQuestionRepository questionRepository = new QuestionRepository();
		IDialogLogic dialogLogic = new DialogLogic(questionRepository);
		IConsole console = new Console(dialogLogic);

		console.start();
		console.runQuizCycle();
		console.stop();
	}
}

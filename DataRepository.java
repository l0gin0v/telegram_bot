import java.util.Map.Entry;
import  java.util.*;

public class DataRepository {
    private final List<Entry<String, Integer>> QA;

    public DataRepository() {
        QA = new ArrayList<>();
        QA.add(Map.entry("1 + 1 = ?", 2));
        QA.add(Map.entry("4 + 5 = ?", 9));
        QA.add(Map.entry("33 + 28 = ?", 61));
    }

    public Entry<String, Integer> getRandomQA() {
        if (QA.isEmpty()) return null;

        Random random = new Random();
        int randomIndex = random.nextInt(QA.size());
        return QA.get(randomIndex);
    }
}
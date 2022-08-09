import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.glassfish.grizzly.utils.Pair;

public class UserAnswer {
    DataBaseHandler dbHandler = new DataBaseHandler();
    private final HashMap<Integer, Pair<String, Boolean>> answers;
    private final HashMap<Integer, Pair<String, Boolean>> userAnswers = new HashMap<>();
    private boolean isAnswered = false;
    private int numberTrueAnswer = 0;
    private int numberPressedButtons = 0;
    private final List<Integer> chosenAnswers = new ArrayList<>();

    UserAnswer(int questionId) {
        answers = dbHandler.getAnswersAtQuestion(questionId);
        for (Map.Entry<Integer, Pair<String, Boolean>> entry : answers.entrySet()) {
            userAnswers.put(entry.getKey(), new Pair<>(entry.getValue().getFirst(), false));
            if (entry.getValue().getSecond()) {
                numberTrueAnswer++;
            }
        }
    }

    public void changeUserAnswer(Integer answerId) {
        for (Map.Entry<Integer, Pair<String, Boolean>> entry : answers.entrySet()) {
            if (Objects.equals(entry.getKey(), answerId)) {
                if (!userAnswers.get(answerId).getSecond()) {
                    userAnswers.put(answerId, new Pair<>(entry.getValue().getFirst(), true));
                    chosenAnswers.add(answerId);
                    numberPressedButtons++;
                } else {
                    userAnswers.put(answerId, new Pair<>(entry.getValue().getFirst(), false));
                    chosenAnswers.remove(answerId);
                    numberPressedButtons--;
                }
            }
        }
    }

    public int getPoints() {
        int points = 0;
        Integer key;
        for (Map.Entry<Integer, Pair<String, Boolean>> entry : userAnswers.entrySet()) {
            key = entry.getKey();
            if (answers.get(key).getSecond() && userAnswers.get(key).getSecond()) {
                points++;
            }
        }
        return points;
    }

    public boolean isFullyCorrect() {
        int points = 0;
        Integer key;
        for (Map.Entry<Integer, Pair<String, Boolean>> entry : userAnswers.entrySet()) {
            key = entry.getKey();
            if (answers.get(key).getSecond() && userAnswers.get(key).getSecond()) {
                points++;
            }
        }
        return points == numberTrueAnswer;
    }

    public void setAnswered(boolean isAnswered) {
        this.isAnswered = isAnswered;
    }

    public boolean isAnswered() {
        return isAnswered;
    }

    public int getNumberTrueAnswers() {
        return numberTrueAnswer;
    }

    public int getNumberPressedButtons() {
        return numberPressedButtons;
    }

    public boolean isAnswered(Integer answerId) {
        for (Map.Entry<Integer, Pair<String, Boolean>> entry : userAnswers.entrySet()) {
            if (entry.getKey().equals(answerId) && entry.getValue().getSecond()) {
                return true;
            }
        }
        return false;
    }

    public List<Integer> getChosenAnswers() {
        return chosenAnswers;
    }

    public HashMap<Integer, Pair<String, Boolean>> getAnswers() {
        return answers;
    }

}

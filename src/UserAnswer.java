import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.glassfish.grizzly.utils.Pair;

public class UserAnswer {
    private final int questionId;
    private final HashMap<Integer, Pair<String, Boolean>> answers;
    private final HashMap<Integer, Pair<String, Boolean>> userAnswers = new HashMap<>();
    private boolean isAnswered = false;
    private int numberTrueAnswer = 0;
    private int numberPressedButtons = 0;
    private final List<Integer> chosenAnswers = new ArrayList<>();
    private String beginningDateTime;
    private String endDateTime;

    UserAnswer(int questionId, DataBaseHandler dbHandler) {
        this.questionId = questionId;
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

    public int getQuestionId(){
        return questionId;
    }

    // Returns id of answers in format "1;2;3"
    public String getUserAnswers(){
        String UserAnswersString = "";
        for (Map.Entry<Integer, Pair<String, Boolean>> entry : answers.entrySet()) {
            if (entry.getValue().getSecond()) {
                UserAnswersString = UserAnswersString.concat(entry.getKey().toString()).concat(";");
            }
        }
        return UserAnswersString;
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

    public void setBeginningDateTime(String beginningDateTime) {
        this.beginningDateTime = beginningDateTime;
    }

    public String getBeginningDateTime() {
        return beginningDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }
}

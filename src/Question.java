import java.util.HashMap;

import org.glassfish.grizzly.utils.Pair;

public record Question(int questionId, int topicId, String questionText,
                       HashMap<Integer, Pair<String, Boolean>> answers, int maxBall) {

    public int getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public HashMap<Integer, Pair<String, Boolean>> getAnswers() {
        return answers;
    }
}

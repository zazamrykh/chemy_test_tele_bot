import java.util.HashMap;

import org.glassfish.grizzly.utils.Pair;

public final class Question {
    private final int questionId;
    private final String questionText;
    private final HashMap<Integer, Pair<String, Boolean>> answers;
    private final int maxBall;

    public Question(int questionId, String questionText,
                    HashMap<Integer, Pair<String, Boolean>> answers, int maxBall) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.answers = answers;
        this.maxBall = maxBall;
    }

    public int getQuestionId() {
        return questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public HashMap<Integer, Pair<String, Boolean>> getAnswers() {
        return answers;
    }

    public int maxBall() {
        return maxBall;
    }
}

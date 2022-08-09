import java.util.HashMap;

import org.glassfish.grizzly.utils.Pair;

public class Question {
    private final int questionId;
    private final int topicId;
    private final String questionText;
    private final HashMap<Integer, Pair<String, Boolean>> answers;

    Question(int questionId, int topicId, String questionText, HashMap<Integer, Pair<String, Boolean>> answers) {
        this.questionId = questionId;
        this.topicId = topicId;
        this.questionText = questionText;
        this.answers = answers;
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getTopicId() {
        return topicId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public HashMap<Integer, Pair<String, Boolean>> getAnswers() {
        return answers;
    }
}

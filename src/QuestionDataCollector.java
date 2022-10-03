import java.util.HashMap;

import org.glassfish.grizzly.utils.Pair;

public final class QuestionDataCollector {
    // moduleIds - id modules where user wants to add question
    private String[] moduleIds;
    // topicIds - id topics relate question
    private String[] topicIds;
    private HashMap<String, Pair<Boolean, Boolean>> answersToQuestion;
    private String questionText;
    private int maxBall;

    public void setModuleIds(String moduleIds) {
        this.moduleIds = moduleIds.split(";");
    }

    public void setTopicIds(String topicIds) {
        this.topicIds = topicIds.split(";");
    }

    public void setAnswersToQuestion(String answersToQuestion) {
        String[] param = answersToQuestion.split(";");
        HashMap<String, Pair<Boolean, Boolean>> answerIsCorrectIsHandwritten = new HashMap<>();
        for (int i = 0; i < param.length - 1; i += 2) {
            String isCorrectString = param[i + 1];
            boolean isCorrect;
            isCorrect = isCorrectString.equals("Верный");
            answerIsCorrectIsHandwritten.put(param[i], new Pair<>(isCorrect, false));
        }
        this.answersToQuestion = answerIsCorrectIsHandwritten;
    }

    public String[] getModuleIds() {
        return moduleIds;
    }

    public String[] getTopicIds() {
        return topicIds;
    }

    public HashMap<String, Pair<Boolean, Boolean>> getAnswersToQuestion() {
        return answersToQuestion;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setMaxBall(int maxBall) {
        this.maxBall = maxBall;
    }

    public int getMaxBall() {
        return maxBall;
    }
}

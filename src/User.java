import java.util.HashMap;

import org.glassfish.grizzly.utils.Pair;


public class User {
    private UserCondition userCondition;
    private String[] moduleIds;
    private String[] topicIds;
    private String questionText;
    private String testNumber;
    private HashMap<String, Pair<Boolean, Boolean>> answers;

    User() {
        userCondition = UserCondition.DOING_NOTHING;
    }

    User(UserCondition userCondition) {
        this.userCondition = userCondition;
    }

    public UserCondition getUserCondition() {
        return userCondition;
    }

    public void setUserCondition(UserCondition userCondition) {
        this.userCondition = userCondition;
    }

    public void setModuleIds(String moduleIds) {
        this.moduleIds = moduleIds.split(";");
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setTestNumber(String testNumber) {
        this.testNumber = testNumber;
    }

    public void setTopicIds(String topicIds) {
        this.topicIds = topicIds.split(";");
    }

    public void setAnswers(String answers) {
        System.out.println("setting answers");
        String[] param = answers.split(";");
        HashMap<String, Pair<Boolean, Boolean>> answerIsCorrectIsHandwritten = new HashMap<>();
        for (int i = 0; i < param.length - 1; i += 2) {
            String isCorrectString = param[i + 1];
            boolean isCorrect;
            isCorrect = isCorrectString.equals("Верный");
            answerIsCorrectIsHandwritten.put(param[i], new Pair<>(isCorrect, false));
        }
        this.answers = answerIsCorrectIsHandwritten;
        System.out.println("answers was setted");
    }

    public String getWarning() {
        return null;
    }

    public void addQuestionToDB() {
        DataBaseHandler dbHandler = new DataBaseHandler();
        dbHandler.addQuestionWithAnswers(moduleIds, topicIds, questionText, "-1", answers);
    }
}

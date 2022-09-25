import java.util.HashMap;
import java.util.List;

import org.glassfish.grizzly.utils.Pair;


public class User {
    private final long chatId;
    private UserCondition userCondition;
    // moduleIds - id modules where user wants to add question
    private String[] moduleIds;
    // topicIds - id topics relate question
    private String[] topicIds;
    private HashMap<String, Pair<Boolean, Boolean>> answersToQuestion;
    private Pair<Integer, String> topic;
    private String login;
    private String password;
    private boolean isAdmin;
    private Testing testing;
    private String questionText;

    User(long chatId) {
        userCondition = UserCondition.DOING_NOTHING;
        this.chatId = chatId;
    }

    public UserCondition getUserCondition() {
        return userCondition;
    }

    public long getChatId() {
        return chatId;
    }

    public void setUserCondition(UserCondition userCondition) {
        this.userCondition = userCondition;
    }

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

    public void setQuestions(List<Question> questions) {
        testing.setQuestions(questions);
    }

    public Question getCurrentQuestion() {
        return testing.getCurrentQuestion();
    }

    public void incrementIdCurrentQuestion() {
        testing.incrementIdCurrentQuestion();
    }

    public void setTopic(Pair<Integer, String> topic) {
        this.topic = topic;
    }

    public Pair<Integer, String> getTopic() {
        return topic;
    }

    public int getPoints() {
        return testing.getPoints();
    }

    public boolean isRegistered() {
        DataBaseHandler dbHandler = new DataBaseHandler();
        return dbHandler.isUserRegistered(String.valueOf(chatId));
    }

    public boolean register(long chatId) {
        DataBaseHandler dbHandler = new DataBaseHandler();
        return dbHandler.addStudent(chatId, login, password, isAdmin);
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean checkAccessKey(String accessKey) {
        DataBaseHandler dbHandler = new DataBaseHandler();
        return dbHandler.checkAccessKey(accessKey);
    }

    public boolean tryMakeAdmin(long chatId, String keyCode) {
        DataBaseHandler dbHandler = new DataBaseHandler();
        return dbHandler.tryMakeAdmin(chatId, keyCode);
    }

    public boolean checkIsAdmin(long chatId) {
        if (!isRegistered()) {
            return false;
        }
        DataBaseHandler dbHandler = new DataBaseHandler();
        return dbHandler.checkIsAdmin(chatId);
    }

    public Testing getTesting() {
        return testing;
    }

    public void setTesting(Testing testing) {
        this.testing = testing;
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

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionText(){
        return questionText;
    }
}

import java.util.HashMap;
import java.util.List;

import org.glassfish.grizzly.utils.Pair;


public class User {
    private long chatId;
    private UserCondition userCondition;
    // moduleIds - id modules where user wants to add question
    private String[] moduleIds;
    // topicIds - id topics relate question
    private String[] topicIds;
    private String questionText;
    private HashMap<String, Pair<Boolean, Boolean>> answersToQuestion;
    private List<Question> questions;
    private Pair<Integer, String> topic;
    private int points = 0;
    private int idCurrentQuestion = 0;
    private String login;
    private String password;
    private boolean isAdmin;

    User(long chatId) {
        userCondition = UserCondition.DOING_NOTHING;
        this.chatId = chatId;
    }

    User(UserCondition userCondition, long chatId) {
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

    public String getWarning() {
        return null;
    }

    public void addQuestionToDB() {
        DataBaseHandler dbHandler = new DataBaseHandler();
        dbHandler.addQuestionWithAnswers(moduleIds, topicIds, questionText, "-1", answersToQuestion);
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public Question getCurrentQuestion() {
        return questions.get(idCurrentQuestion);
    }

    public void incrementIdCurrentQuestion() {
        idCurrentQuestion++;
    }

    public void resetIdCurrentQuestion() {
        idCurrentQuestion = 1;
    }

    public void incrementPoints() {
        points++;
    }

    public void resetPoints() {
        points = 0;
    }

    public boolean isLastQuestion() {
        return idCurrentQuestion == questions.size() - 1;
    }

    public void setTopic(Pair<Integer, String> topic) {
        this.topic = topic;
    }

    public Pair<Integer, String> getTopic() {
        return topic;
    }

    public int getPoints() {
        return points;
    }

    public boolean isRegistered() {
        DataBaseHandler dbHandler = new DataBaseHandler();
        return dbHandler.isUserRegistered(String.valueOf(chatId));
    }

    public boolean register(long chatId) {
        DataBaseHandler dbHandler = new DataBaseHandler();
        boolean successfulAddStudent = dbHandler.addStudent(login, password);
        if (!successfulAddStudent) {
            dbHandler.deleteStudent(dbHandler.getStudentId(chatId));
            return false;
        }
        return dbHandler.addChat(chatId, login);
    }

    public void setLogin(String login){
        this.login = login;
    }

    public String getLogin(){
        return login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}

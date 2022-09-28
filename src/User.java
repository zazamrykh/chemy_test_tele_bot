public class User {
    private final long chatId;
    private UserCondition userCondition;
    private QuestionDataCollector questionDataCollector;
    private String login;
    private String password;
    private boolean isAdmin;
    private Testing testing;

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

    public QuestionDataCollector getQuestionDataCollector() {
        return questionDataCollector;
    }

    public void createQuestionDataCollector() {
        questionDataCollector = new QuestionDataCollector();
    }

    public Question getCurrentQuestion() {
        return testing.getCurrentQuestion();
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
}

public class User {
    private final long chatId;
    private UserCondition userCondition;
    private QuestionDataCollector questionDataCollector;
    private final RegistrationManager registrationManager;
    private Testing testing;

    User(long chatId) {
        userCondition = UserCondition.DOING_NOTHING;
        this.chatId = chatId;
        registrationManager = new RegistrationManager(chatId);
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

    public Testing getTesting() {
        return testing;
    }

    public void setTesting(Testing testing) {
        this.testing = testing;
    }

    public RegistrationManager getRegistrationManager() {
        return registrationManager;
    }
}

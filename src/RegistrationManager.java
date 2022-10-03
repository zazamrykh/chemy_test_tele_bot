public final class RegistrationManager {
    private String login;
    private final long chatId;
    private boolean isAdmin;

    public RegistrationManager(long chatId) {
        this.chatId = chatId;
    }

    public boolean register(){
        DataBaseHandler dbHandler = new DataBaseHandler();
        return dbHandler.addStudent(chatId, login, isAdmin);
    }

    public void setLogin(String messageText) {
        this.login = messageText;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public boolean isRegistered() {
        DataBaseHandler dbHandler = new DataBaseHandler();
        return dbHandler.isUserRegistered(String.valueOf(chatId));
    }

    public boolean tryMakeAdmin(Long chatId, String keyCode) {
        DataBaseHandler dbHandler = new DataBaseHandler();
        return dbHandler.tryMakeAdmin(chatId, keyCode);
    }

    public boolean checkAccessKey(String accessKey) {
        DataBaseHandler dbHandler = new DataBaseHandler();
        return dbHandler.checkAccessKey(accessKey);
    }

    public boolean checkIsAdmin(long chatId) {
        if (!isRegistered()) {
            return false;
        }
        DataBaseHandler dbHandler = new DataBaseHandler();
        return dbHandler.checkIsAdmin(chatId);
    }
}

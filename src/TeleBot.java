import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.glassfish.grizzly.utils.Pair;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class TeleBot extends TelegramLongPollingBot {
    // HashMap users keep data about users conditions in bot conversation
    // Key - ChatId, Value - Class User with information about user
    private final HashMap<Long, User> users = new HashMap<>();

    private final DataBaseHandler dbHandler;
    // HashMap userAnswers keep data about answer user at question

    public TeleBot(DataBaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    @Override
    public String getBotUsername() {
        return TelegramConfig.botUsername;
    }

    @Override
    public String getBotToken() {
        return TelegramConfig.botToken;
    }

    public static void main(String[] args) throws TelegramApiException {
        TeleBot bot = new TeleBot(new DataBaseHandler());
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                handleMessage(update.getMessage());
            } else if (update.hasCallbackQuery()) {
                handleAnswer(update.getCallbackQuery());
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Message message) throws TelegramApiException {
        if (!message.hasText()) {
            return;
        }
        Long chatId = message.getChatId();
        if (!users.containsKey(chatId)) {
            users.put(chatId, new User(chatId));
        }

        if (!message.hasEntities()) {
            handleTextMessage(message);
            return;
        }

        Optional<MessageEntity> commandEntity =
                message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();

        if (commandEntity.isEmpty()) {
            return;
        }

        String command = message.getText().substring(commandEntity.get().getOffset(),
                commandEntity.get().getLength());

        User currentUser = users.get(chatId);

        handleCommand(command, currentUser);
    }

    private void handleCommand(String command, User currentUser) throws TelegramApiException {
        long chatId = currentUser.getChatId();
        if (command.equals(Commands.Register)) {
            if (currentUser.isRegistered()) {
                sendMessage(BotMessages.UserIsAlreadyRegistered, chatId);
                return;
            }
            sendMessage(BotMessages.EnterYourName, chatId);
            currentUser.setUserCondition(UserCondition.ENTERING_LOGIN);
        }

        if (!currentUser.isRegistered()) {
            sendMessage(BotMessages.YouAreNotRegistered, chatId);
            return;
        }

        switch (command) {
            // Treatment of admin commands
            case Commands.AddQuestion -> {
                if (!currentUser.checkIsAdmin(chatId)) {
                    sendMessage(BotMessages.YouDoNotHaveAccessToAdminCommands, chatId);
                    return;
                }
                currentUser.createQuestionDataCollector();
                currentUser.setUserCondition(UserCondition.ENTERING_MODULE);
                sendMessage(BotMessages.AddQuestionMessage, chatId);
            }
            case Commands.AddModule -> {
                if (!currentUser.checkIsAdmin(chatId)) {
                    sendMessage(BotMessages.YouDoNotHaveAccessToAdminCommands, chatId);
                    return;
                }
                currentUser.setUserCondition(UserCondition.ADDING_MODULE);
                sendMessage(BotMessages.AddModuleMessage, chatId);
            }
            case Commands.AddTopic -> {
                if (!currentUser.checkIsAdmin(chatId)) {
                    sendMessage(BotMessages.YouDoNotHaveAccessToAdminCommands, chatId);
                    return;
                }
                currentUser.setUserCondition(UserCondition.ADDING_TOPIC);
                sendMessage(BotMessages.AddTopicMessage, chatId);
            }
            case Commands.Format -> {
                if (!currentUser.checkIsAdmin(chatId)) {
                    sendMessage(BotMessages.YouDoNotHaveAccessToAdminCommands, chatId);
                    return;
                }
                sendMessage(BotMessages.FormatMessage, chatId);
            }
            case Commands.GetModules -> {
                HashMap<Integer, String> modules = dbHandler.getModules();
                for (Map.Entry<Integer, String> entry : modules.entrySet()) {
                    sendMessage(entry.getKey().toString() + ". " + entry.getValue(), chatId);
                }
            }
            case Commands.GetTopics -> {
                List<Pair<Integer, String>> topics = dbHandler.getTopics();
                Integer moduleId = -1;
                for (Pair<Integer, String> topic : topics) {
                    Pair<Integer, String> module = dbHandler.getModule(topic.getFirst());
                    if (!moduleId.equals(module.getFirst())) {
                        sendMessage("Тема " + module.getFirst().toString() + ". " + module.getSecond() + ":",
                                chatId);
                        moduleId = module.getFirst();
                    }
                    sendMessage(topic.getFirst() + ". " + topic.getSecond(), chatId);
                }
            }
            case Commands.GetTopicTest -> {
                currentUser.setUserCondition(UserCondition.ENTERING_MODULE_FOR_GETTING_TEST);
                List<List<InlineKeyboardButton>> buttons = getButtonsForChoosingTestingModule(dbHandler.getModules());
                execute(SendMessage.builder()
                        .text(BotMessages.PressingModuleForGettingTest)
                        .chatId(chatId)
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                        .build());

            }
            case Commands.EnterKeyCode -> {
                if (currentUser.checkIsAdmin(chatId)) {
                    sendMessage(BotMessages.YouAreAlreadyAdmin, chatId);
                    return;
                }
                sendMessage(BotMessages.EnterKeyCode, chatId);
                currentUser.setUserCondition(UserCondition.ENTERING_ACCESS_KEY_AFTER_REGISTRATION);
            }
        }
    }

    private void handleTextMessage(Message message) throws TelegramApiException {
        Long chatId = message.getChatId();
        String messageText = message.getText();
        User currentUser = users.get(chatId);
        UserCondition currentCondition = currentUser.getUserCondition();
        switch (currentCondition) {
            case ENTERING_MODULE -> {
                currentUser.getQuestionDataCollector().setModuleIds(messageText);
                sendMessage(BotMessages.EnteringModuleMessage, chatId);
                currentUser.setUserCondition(UserCondition.ENTERING_TOPIC);
            }
            case ENTERING_TOPIC -> {
                currentUser.getQuestionDataCollector().setTopicIds(messageText);
                sendMessage(BotMessages.EnteringTopicMessage, chatId);
                currentUser.setUserCondition(UserCondition.ENTERING_QUESTION);
            }
            case ENTERING_QUESTION -> {
                currentUser.getQuestionDataCollector().setQuestionText(messageText);
                sendMessage(BotMessages.EnteringQuestionMessage, chatId);
                currentUser.setUserCondition(UserCondition.ENTERING_ANSWERS);
            }
            case ENTERING_ANSWERS -> {
                QuestionDataCollector questionDataCollector = currentUser.getQuestionDataCollector();
                questionDataCollector.setAnswersToQuestion(messageText);
                sendMessage(BotMessages.EnterMaxBalls, chatId);
                currentUser.setUserCondition(UserCondition.ENTERING_MAX_BALL);
            }
            case ENTERING_MAX_BALL -> {
                QuestionDataCollector questionDataCollector = currentUser.getQuestionDataCollector();
                questionDataCollector.setMaxBall(Integer.parseInt(messageText));
                sendMessage(BotMessages.AnswerWasAdded, chatId);
                dbHandler.addQuestionWithAnswers(questionDataCollector.getModuleIds(),
                        questionDataCollector.getTopicIds(),
                        questionDataCollector.getQuestionText(), "-1",
                        questionDataCollector.getAnswersToQuestion(),
                        questionDataCollector.getMaxBall());
            }
            case ADDING_MODULE -> {
                dbHandler.addModule(messageText);
                currentUser.setUserCondition(UserCondition.DOING_NOTHING);
                sendMessage(BotMessages.AddingModuleMessage, chatId);
            }
            case ADDING_TOPIC -> {
                String[] param = messageText.split(";");
                String topicName = param[0];
                int moduleId = Integer.parseInt(param[1]);
                dbHandler.addTopic(topicName, moduleId);
                currentUser.setUserCondition(UserCondition.DOING_NOTHING);
                sendMessage(BotMessages.AddingTopicMessage, chatId);
            }
            case ENTERING_LOGIN -> {
                currentUser.setLogin(messageText);
                sendMessage(BotMessages.EnterPassword, chatId);
                currentUser.setUserCondition(UserCondition.ENTERING_PASSWORD);
            }
            case ENTERING_PASSWORD -> {
                currentUser.setPassword(messageText);
                List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                buttons.add(List.of(InlineKeyboardButton.builder()
                        .text(BotMessages.Yes).callbackData(CallbackData.AnswerIsAdmin + ":" +
                                "true").build()));
                buttons.add(List.of(InlineKeyboardButton.builder()
                        .text(BotMessages.No).callbackData(CallbackData.AnswerIsAdmin + ":" +
                                "false").build()));

                execute(SendMessage.builder()
                        .text(BotMessages.AreYouAdmin)
                        .chatId(chatId)
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                        .build());

                currentUser.setUserCondition(UserCondition.ANSWERING_IS_ADMIN);
            }
            case ANSWERING_IS_ADMIN -> sendMessage(BotMessages.AnswerUpperQuestion, chatId);
            case ENTERING_ACCESS_KEY -> {
                if (currentUser.checkAccessKey(messageText)) {
                    currentUser.setIsAdmin(true);
                    currentUser.register(chatId);
                    sendMessage(BotMessages.SuccessfulRegistration, chatId);
                } else {
                    sendMessage(BotMessages.WrongKeyCode, chatId);
                }

            }
            case ENTERING_ACCESS_KEY_AFTER_REGISTRATION -> {
                if (currentUser.tryMakeAdmin(chatId, messageText)) {
                    sendMessage(BotMessages.CorrectKeyCode, chatId);
                } else {
                    sendMessage(BotMessages.WrongKeyCodeAgterRegistration, chatId);
                }
            }
        }
    }

    private void handleAnswer(CallbackQuery callbackQuery) throws TelegramApiException {
        Message message = callbackQuery.getMessage();
        String[] callbackData = callbackQuery.getData().split(":");
        String action = callbackData[0];
        Long chatId = message.getChatId();
        switch (action) {
            case CallbackData.PressOption -> {
                Integer userAnswerNumber = Integer.parseInt(callbackData[1]);
                User currentUser = users.get(chatId);
                Testing currentTesting = currentUser.getTesting();
                UserAnswer userAnswer = currentTesting.getUserAnswer(userAnswerNumber);
                if (userAnswer.isAnswered()) {
                    return;
                }
                Integer answerId = Integer.valueOf(callbackData[2]);
                if (userAnswer.isAnswered(answerId)) {
                    userAnswer.changeUserAnswer(answerId);
                    execute(EditMessageReplyMarkup.builder()
                            .chatId(chatId)
                            .messageId(message.getMessageId())
                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(getButtons(userAnswer,
                                    userAnswer.getChosenAnswers(), userAnswerNumber)).build())
                            .build());
                } else if (userAnswer.getNumberPressedButtons() < userAnswer.getNumberTrueAnswers()) {
                    userAnswer.changeUserAnswer(answerId);

                    execute(EditMessageReplyMarkup.builder()
                            .chatId(chatId)
                            .messageId(message.getMessageId())
                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(getButtons(userAnswer,
                                    userAnswer.getChosenAnswers(), userAnswerNumber)).build())
                            .build());
                }
            }
            case CallbackData.PressAnswer -> {
                Integer userAnswerNumber = Integer.parseInt(callbackData[1]);
                User currentUser = users.get(chatId);
                Testing currentTesting = currentUser.getTesting();

                UserAnswer userAnswer = currentTesting.getUserAnswer(userAnswerNumber);
                if (userAnswer.isAnswered()) {
                    return;
                }
                sendMessage(BotMessages.CollectedBalls + userAnswer.getPoints() + "/" +
                        userAnswer.getNumberTrueAnswers(), chatId);
                userAnswer.setEndDateTime(convertDateToSqlFormat(message.getDate()));
                userAnswer.setAnswered(true);
                dbHandler.addStudentAnswer(currentUser, userAnswer);
                if (currentUser.getUserCondition().equals(UserCondition.SOLVING_TEST)) {
                    if (userAnswer.isFullyCorrect()) {
                        currentTesting.incrementPoints();
                    }
                    if (currentTesting.isLastQuestion()) {
                        currentTesting.setPoints(currentUser.getPoints());
                        currentTesting.setEndDateTime(convertDateToSqlFormat(message.getDate()));
                        dbHandler.addTesting(currentTesting);
                        sendMessage(BotMessages.CollectedForTestBalls + " \"" +
                                currentTesting.getTopicName() + "\": " + currentUser.getPoints(), chatId);
                    } else {
                        currentTesting.incrementIdCurrentQuestion();
                        sendQuestion(currentUser.getCurrentQuestion(), currentUser, message.getDate(),
                                currentTesting.getUserAnswerNumber());
                    }
                }
            }
            case CallbackData.ChooseModule -> {
                User currentUser = users.get(chatId);
                if (!currentUser.getUserCondition().equals(UserCondition.ENTERING_MODULE_FOR_GETTING_TEST)) {
                    return;
                }
                currentUser.setUserCondition(UserCondition.ENTERING_TOPIC_FOR_GETTING_TEST);
                Integer moduleId = Integer.parseInt(callbackData[1]);
                List<List<InlineKeyboardButton>> buttons = getButtonsForChoosingTestingTopic(dbHandler.getTopics(moduleId));
                execute(SendMessage.builder()
                        .text(BotMessages.PressingTopicForGettingTest)
                        .chatId(chatId)
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                        .build());
            }
            case CallbackData.ChooseTopic -> {
                User currentUser = users.get(chatId);
                if (!currentUser.getUserCondition().equals(UserCondition.ENTERING_TOPIC_FOR_GETTING_TEST)) {
                    return;
                }
                currentUser.setUserCondition(UserCondition.SOLVING_TEST);
                int topicId = Integer.parseInt(callbackData[1]);
                String topicName = dbHandler.getTopicName(topicId);
                Testing currentTesting = new Testing(chatId, convertDateToSqlFormat(message.getDate()),
                        topicId, topicName, dbHandler.getQuestions(topicId));
                currentUser.setTesting(currentTesting);
                sendQuestion(currentUser.getCurrentQuestion(), currentUser, message.getDate(),
                        currentTesting.getUserAnswerNumber());
            }
            case CallbackData.AnswerIsAdmin -> {
                User currentUser = users.get(chatId);
                boolean isAdmin = Boolean.parseBoolean(callbackData[1]);
                if (isAdmin) {
                    sendMessage(BotMessages.EnterKeyCode, chatId);
                    currentUser.setUserCondition(UserCondition.ENTERING_ACCESS_KEY);
                } else {
                    currentUser.setIsAdmin(false);
                    if (currentUser.register(chatId)) {
                        sendMessage(BotMessages.SuccessfulRegistration, chatId);
                    } else {
                        sendMessage(BotMessages.RegistrationError, chatId);
                    }
                }
            }
        }
    }

    private void sendQuestion(Question question, User user, Integer sendDateTime,
                              Integer userAnswerNumber) throws TelegramApiException {
        long chatId = user.getChatId();
        Testing testing = user.getTesting();
        testing.addUserAnswer(question.getQuestionId(), convertDateToSqlFormat(sendDateTime), dbHandler);
        UserAnswer userAnswer = testing.getUserAnswer(userAnswerNumber);
        userAnswer.setBeginningDateTime(convertDateToSqlFormat(sendDateTime));
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Integer answerId;
        String answerText;
        Boolean isAnswerCorrect;
        HashMap<Integer, Pair<String, Boolean>> answers = question.getAnswers();
        for (Map.Entry<Integer, Pair<String, Boolean>> entry : answers.entrySet()) {
            answerId = entry.getKey();
            answerText = entry.getValue().getFirst();
            isAnswerCorrect = entry.getValue().getSecond();
            buttons.add(List.of(InlineKeyboardButton.builder()
                    .text(answerText).callbackData(CallbackData.PressOption + ":" +
                            userAnswerNumber + ":" + answerId + ":" + isAnswerCorrect).build()));
        }
        buttons.add(List.of(InlineKeyboardButton.builder()
                .text("Ответить").callbackData(CallbackData.PressAnswer + ":" + userAnswerNumber).build()));

        execute(SendMessage.builder()
                .text(question.getQuestionText())
                .chatId(chatId)
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                .build());
    }

    private void sendMessage(String messageText, Long chatId) throws TelegramApiException {
        execute(SendMessage.builder()
                .text(messageText)
                .chatId(chatId)
                .build());
    }

    private List<List<InlineKeyboardButton>> getButtons(UserAnswer userAnswer, List<Integer> idChosenAnswers,
                                                        Integer userAnswerNumber) {
        HashMap<Integer, Pair<String, Boolean>> answers = userAnswer.getAnswers();
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Integer answerId;
        String answerText;
        Boolean isAnswerCorrect;
        for (Map.Entry<Integer, Pair<String, Boolean>> entry : answers.entrySet()) {
            answerId = entry.getKey();
            if (idChosenAnswers.contains(answerId)) {
                answerText = entry.getValue().getFirst() + " \uD83E\uDDE0";
            } else {
                answerText = entry.getValue().getFirst();
            }
            isAnswerCorrect = entry.getValue().getSecond();
            buttons.add(List.of(InlineKeyboardButton.builder()
                    .text(answerText).callbackData(CallbackData.PressOption + ":" +
                            userAnswerNumber + ":" + answerId + ":" + isAnswerCorrect).build()));
        }
        buttons.add(List.of(InlineKeyboardButton.builder()
                .text("Ответить").callbackData(CallbackData.PressAnswer + ":" + userAnswerNumber).build()));
        return buttons;
    }

    private List<List<InlineKeyboardButton>> getButtonsForChoosingTestingModule(HashMap<Integer, String> modules) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Integer moduleId;
        for (Map.Entry<Integer, String> module : modules.entrySet()) {
            moduleId = module.getKey();
            buttons.add(List.of(InlineKeyboardButton.builder()
                    .text(moduleId + ". " + module.getValue()).callbackData(CallbackData.ChooseModule + ":" +
                            moduleId).build()));
        }
        return buttons;
    }

    private List<List<InlineKeyboardButton>> getButtonsForChoosingTestingTopic(HashMap<Integer, String> topics) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Integer topicId;
        for (Map.Entry<Integer, String> topic : topics.entrySet()) {
            topicId = topic.getKey();
            buttons.add(List.of(InlineKeyboardButton.builder()
                    .text(topicId + ". " + topic.getValue()).callbackData(CallbackData.ChooseTopic + ":" +
                            topicId).build()));
        }
        return buttons;
    }

    private static Date convertUnixTimestampToDate(Integer timestamp) {
        return new Date(timestamp * 1000L);
    }

    private static String convertDateToSqlFormat(Integer dateTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(convertUnixTimestampToDate(dateTime));
    }
}

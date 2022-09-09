import java.util.ArrayList;
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

import lombok.SneakyThrows;

public class TeleBot extends TelegramLongPollingBot {
    // HashMap users keep data about users conditions in bot conversation
    // Key - ChatId, Value - Class User with information about user
    private final HashMap<Long, User> users = new HashMap<>();

    // HashMap userAnswers keep data about answer user at question
    private final HashMap<Integer, UserAnswer> userAnswers = new HashMap<>();

    private Integer userAnswerId = -1;

    @Override
    public String getBotUsername() {
        return TelegramConfig.botUsername;
    }

    @Override
    public String getBotToken() {
        return TelegramConfig.botToken;
    }

    @SneakyThrows
    public static void main(String[] args) {
        TeleBot bot = new TeleBot();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleAnswer(update.getCallbackQuery());
        }
    }

    @SneakyThrows
    private void handleMessage(Message message) {
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
        DataBaseHandler dbHandler = new DataBaseHandler();
        switch (command) {
            case "/add_question" -> {
                currentUser.setUserCondition(UserCondition.ENTERING_MODULE);
                sendMessage(BotMessages.AddQuestionMessage, chatId);
            }
            case "/get_random_question" -> {
                userAnswerId++;
                int questionId = dbHandler.getRandomQuestionId();
                userAnswers.put(userAnswerId, new UserAnswer(questionId));

                List<List<InlineKeyboardButton>> buttons;
                buttons = getButtons(userAnswerId, userAnswers.get(userAnswerId).getChosenAnswers());

                String questionText = dbHandler.getQuestion(questionId);
                execute(SendMessage.builder()
                        .text(questionText)
                        .chatId(chatId)
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                        .build());
            }
            case "/get_all_questions" -> {
                List<String> allQuestions = dbHandler.getAllQuestions();
                for (int i = 0; i < allQuestions.size(); i++) {
                    sendMessage(i + " " + allQuestions.get(i), chatId);
                }
            }
            case "/get_modules" -> {
                HashMap<Integer, String> modules = dbHandler.getModules();
                for (Map.Entry<Integer, String> entry : modules.entrySet()) {
                    sendMessage(entry.getKey().toString() + ". " + entry.getValue(), chatId);
                }
            }
            case "/get_topics" -> {
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
            case "/get_topic_test" -> {
                if (!currentUser.isRegistered()) {
                    sendMessage("Вы не зарегистрированы. Введите '/register' чтобы зарегистрироваться.",
                            chatId);
                }
                currentUser.setUserCondition(UserCondition.ENTERING_MODULE_FOR_GETTING_TEST);
                List<List<InlineKeyboardButton>> buttons = getButtonsForChoosingTestingModule(dbHandler.getModules());
                execute(SendMessage.builder()
                        .text(BotMessages.PressingModuleForGettingTest)
                        .chatId(chatId)
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                        .build());

            }
            case "/add_module" -> {
                currentUser.setUserCondition(UserCondition.ADDING_MODULE);
                sendMessage(BotMessages.AddModuleMessage, chatId);
            }
            case "/add_topic" -> {
                currentUser.setUserCondition(UserCondition.ADDING_TOPIC);
                sendMessage(BotMessages.AddTopicMessage, chatId);
            }
            case "/format" -> sendMessage(BotMessages.FormatMessage, chatId);
            case "/register" -> {
                if (currentUser.isRegistered()){
                    sendMessage("Пользователь уже зарегестрирован.", chatId);
                    return;
                }
                sendMessage("Введите ваше имя", chatId);
                currentUser.setUserCondition(UserCondition.ENTERING_LOGIN);
            }
        }
    }

    @SneakyThrows
    private void handleTextMessage(Message message) {
        Long chatId = message.getChatId();
        String messageText = message.getText();
        User currentUser = users.get(chatId);
        UserCondition currentCondition = currentUser.getUserCondition();
        switch (currentCondition) {
            case ENTERING_MODULE -> {
                currentUser.setModuleIds(messageText);
                sendMessage(BotMessages.EnteringModuleMessage, chatId);
                currentUser.setUserCondition(UserCondition.ENTERING_TOPIC);
            }
            case ENTERING_TOPIC -> {
                currentUser.setTopicIds(messageText);
                sendMessage(BotMessages.EnteringTopicMessage, chatId);
                currentUser.setUserCondition(UserCondition.ENTERING_QUESTION);
            }
            case ENTERING_QUESTION -> {
                currentUser.setQuestionText(messageText);
                sendMessage(BotMessages.EnteringQuestionMessage, chatId);
                currentUser.setUserCondition(UserCondition.ENTERING_ANSWERS);
            }
            case ENTERING_ANSWERS -> {
                currentUser.setAnswersToQuestion(messageText);
                currentUser.addQuestionToDB();
                sendMessage(BotMessages.EnteringAnswersMessage, chatId);
                currentUser.setUserCondition(UserCondition.DOING_NOTHING);
            }
            case ADDING_MODULE -> {
                DataBaseHandler dbHandler = new DataBaseHandler();
                dbHandler.addModule(messageText);
                currentUser.setUserCondition(UserCondition.DOING_NOTHING);
                sendMessage(BotMessages.AddingModuleMessage, chatId);
            }
            case ADDING_TOPIC -> {
                DataBaseHandler dbHandler = new DataBaseHandler();
                String[] param = messageText.split(";");
                String topicName = param[0];
                int moduleId = Integer.parseInt(param[1]);
                dbHandler.addTopic(topicName, moduleId);
                currentUser.setUserCondition(UserCondition.DOING_NOTHING);
                sendMessage(BotMessages.AddingTopicMessage, chatId);
            }
            case ENTERING_LOGIN -> {
                currentUser.setLogin(messageText);
                sendMessage("Введите пароль.", chatId);
                currentUser.setUserCondition(UserCondition.ENTERING_PASSWORD);
            }
            case ENTERING_PASSWORD -> {
                currentUser.setPassword(messageText);
                List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
                buttons.add(List.of(InlineKeyboardButton.builder()
                        .text("Да").callbackData(CallBackData.AnswerIsAdmin + ":" +
                                "true").build()));
                buttons.add(List.of(InlineKeyboardButton.builder()
                        .text("Нет").callbackData(CallBackData.AnswerIsAdmin + ":" +
                                "false").build()));

                execute(SendMessage.builder()
                        .text(BotMessages.AreYouAdmin)
                        .chatId(chatId)
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                        .build());

                if (currentUser.register(chatId)){
                    sendMessage("Успешная регистрация", chatId);
                } else{
                    sendMessage("Ошибка регистрации", chatId);
                }
                currentUser.setUserCondition(UserCondition.DOING_NOTHING);
            }
        }
    }

    private void handleAnswer(CallbackQuery callbackQuery) throws TelegramApiException {
        Message message = callbackQuery.getMessage();
        String[] callbackData = callbackQuery.getData().split(":");
        String action = callbackData[0];
        Long chatId = message.getChatId();
        switch (action) {
            case CallBackData.PressOption -> {
                Integer userAnswerId = Integer.parseInt(callbackData[1]);
                UserAnswer userAnswer = userAnswers.get(userAnswerId);
                if (userAnswer.isAnswered()) {
                    return;
                }
                Integer answerId = Integer.valueOf(callbackData[2]);
                if (userAnswer.isAnswered(answerId)) {
                    userAnswer.changeUserAnswer(answerId);
                    execute(EditMessageReplyMarkup.builder()
                            .chatId(chatId)
                            .messageId(message.getMessageId())
                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(getButtons(userAnswerId,
                                    userAnswer.getChosenAnswers())).build())
                            .build());
                } else if (userAnswer.getNumberPressedButtons() < userAnswer.getNumberTrueAnswers()) {
                    userAnswer.changeUserAnswer(answerId);

                    execute(EditMessageReplyMarkup.builder()
                            .chatId(chatId)
                            .messageId(message.getMessageId())
                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(getButtons(userAnswerId,
                                    userAnswer.getChosenAnswers())).build())
                            .build());
                }
            }
            case CallBackData.PressAnswer -> {
                Integer userAnswerId = Integer.parseInt(callbackData[1]);
                UserAnswer userAnswer = userAnswers.get(userAnswerId);
                if (userAnswer.isAnswered()) {
                    return;
                }
                sendMessage("Набранные баллы: " + userAnswer.getPoints() + "/" +
                        userAnswer.getNumberTrueAnswers(), chatId);
                userAnswer.setAnswered(true);
                User currentUser = users.get(chatId);
                if (currentUser.getUserCondition().equals(UserCondition.SOLVING_TEST)) {
                    if (userAnswer.isFullyCorrect()) {
                        currentUser.incrementPoints();
                    }
                    if (currentUser.isLastQuestion()) {
                        sendMessage("Набранные баллы за тест по теме \"" +
                                currentUser.getTopic().getSecond() + "\": " + currentUser.getPoints(), chatId);
                    } else {
                        currentUser.incrementIdCurrentQuestion();
                        sendQuestion(currentUser.getCurrentQuestion(), chatId);
                    }
                }
            }
            case CallBackData.ChooseModule -> {
                User currentUser = users.get(chatId);
                if (!currentUser.getUserCondition().equals(UserCondition.ENTERING_MODULE_FOR_GETTING_TEST)) {
                    return;
                }
                currentUser.setUserCondition(UserCondition.ENTERING_TOPIC_FOR_GETTING_TEST);
                Integer moduleId = Integer.parseInt(callbackData[1]);
                DataBaseHandler dbHandler = new DataBaseHandler();
                List<List<InlineKeyboardButton>> buttons = getButtonsForChoosingTestingTopic(dbHandler.getTopics(moduleId));
                execute(SendMessage.builder()
                        .text(BotMessages.PressingTopicForGettingTest)
                        .chatId(chatId)
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                        .build());
            }
            case CallBackData.ChooseTopic -> {
                User currentUser = users.get(chatId);
                if (!currentUser.getUserCondition().equals(UserCondition.ENTERING_TOPIC_FOR_GETTING_TEST)) {
                    return;
                }
                currentUser.setUserCondition(UserCondition.SOLVING_TEST);
                int topicId = Integer.parseInt(callbackData[1]);
                DataBaseHandler dbHandler = new DataBaseHandler();
                currentUser.setQuestions(dbHandler.getQuestions(topicId));
                currentUser.setTopic(new Pair<>(topicId, dbHandler.getTopicName(topicId)));
                sendQuestion(currentUser.getCurrentQuestion(), chatId);
            }
            case CallBackData.AnswerIsAdmin -> {
                User currentUser = users.get(chatId);
                boolean isAdmin = Boolean.parseBoolean(callbackData[1]);
                if (isAdmin) {
                    sendMessage("Введите ключ доступа.", chatId);
                    currentUser.setUserCondition(UserCondition.ENTERING_ACCESS_KEY);
                } else {
                    currentUser.setIsAdmin(false);
                    //  Нужно изменить бд, сделать доп колонку в таблице студентов, isAdmin и соответственно поменять dbhandler.
                }
            }
        }

    }

    private void sendQuestion(Question question, long chatId) throws TelegramApiException {
        userAnswerId++;
        userAnswers.put(userAnswerId, new UserAnswer(question.getQuestionId()));
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
                    .text(answerText).callbackData(CallBackData.PressOption + ":" +
                            userAnswerId + ":" + answerId + ":" + isAnswerCorrect).build()));
        }
        buttons.add(List.of(InlineKeyboardButton.builder()
                .text("Ответить").callbackData(CallBackData.PressAnswer + ":" + userAnswerId).build()));

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

    private List<List<InlineKeyboardButton>> getButtons(Integer userAnswerId, List<Integer> idChosenAnswers) {
        HashMap<Integer, Pair<String, Boolean>> answers = userAnswers.get(userAnswerId).getAnswers();
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
                    .text(answerText).callbackData(CallBackData.PressOption + ":" +
                            userAnswerId + ":" + answerId + ":" + isAnswerCorrect).build()));
        }
        buttons.add(List.of(InlineKeyboardButton.builder()
                .text("Ответить").callbackData(CallBackData.PressAnswer + ":" + userAnswerId).build()));
        return buttons;
    }

    private List<List<InlineKeyboardButton>> getButtonsForChoosingTestingModule(HashMap<Integer, String> modules) {
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        Integer moduleId;
        for (Map.Entry<Integer, String> module : modules.entrySet()) {
            moduleId = module.getKey();
            buttons.add(List.of(InlineKeyboardButton.builder()
                    .text(moduleId + ". " + module.getValue()).callbackData(CallBackData.ChooseModule + ":" +
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
                    .text(topicId + ". " + topic.getValue()).callbackData(CallBackData.ChooseTopic + ":" +
                            topicId).build()));
        }
        return buttons;
    }
}

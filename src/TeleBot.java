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

        if (!message.hasText()) return;
        String chatId = message.getChatId().toString();

        if (!message.hasEntities()) return;

        Optional<MessageEntity> commandEntity =
                message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();

        if (commandEntity.isPresent()) {
            String command = message.getText().substring(commandEntity.get().getOffset(),
                    commandEntity.get().getLength());

            switch (command) {
                case "/add_question" -> {
                    // Don't do anything yet
                    sendMessage("Напишите свой вопрос.", chatId);
                }
                case "/get_random_question" -> {
                    userAnswerId++;

                    DataBaseHandler dbHandler = new DataBaseHandler();
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
                    DataBaseHandler dbHandler = new DataBaseHandler();
                    List<String> allQuestions = dbHandler.getAllQuestions();
                    for (int i = 0; i < allQuestions.size(); i++) {
                        sendMessage(i + " " + allQuestions.get(i), chatId);
                    }
                }
            }
        }
    }

    @SneakyThrows
    private void handleAnswer(CallbackQuery callbackQuery) {
        Message message = callbackQuery.getMessage();
        String[] param = callbackQuery.getData().split(":");
        String action = param[0];
        Integer userAnswerId = Integer.parseInt(param[1]);
        UserAnswer userAnswer = userAnswers.get(userAnswerId);
        String chatId = String.valueOf(message.getChatId());
        if (userAnswer.isAnswered()) {
            return;
        }
        switch (action) {
            case "Option" -> {
                Integer answerId = Integer.valueOf(param[2]);
                if (userAnswer.isAnswered(answerId)) {
                    userAnswer.changeUserAnswer(answerId);

                    execute(EditMessageReplyMarkup.builder()
                            .chatId(chatId)
                            .messageId(message.getMessageId())
                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(getButtons(userAnswerId,
                                    userAnswer.getChosenAnswers())).build())
                            .build());
                } else if (userAnswer.getNumberPressedButtons() < userAnswer.getNumberTrueAnswer()) {
                    userAnswer.changeUserAnswer(answerId);

                    execute(EditMessageReplyMarkup.builder()
                            .chatId(chatId)
                            .messageId(message.getMessageId())
                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(getButtons(userAnswerId,
                                    userAnswer.getChosenAnswers())).build())
                            .build());
                }
            }
            case "Answer" -> {
                sendMessage("Набранные баллы: " + userAnswer.getPoints(), chatId);
                userAnswer.setAnswered(true);
            }
        }

    }

    private void sendMessage(String messageText, String chatId) throws TelegramApiException {
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
                    .text(answerText).callbackData("Option:" +
                            userAnswerId + ":" + answerId + ":" + isAnswerCorrect).build()));
        }
        buttons.add(List.of(InlineKeyboardButton.builder()
                .text("Ответить").callbackData("Answer:" + userAnswerId).build()));
        return buttons;
    }
}

public class BotMessages {
    public static final String AddQuestionMessage = """
                        Введите номера модулей, к которым будет относиться вопрос.
                        Пример оформления: '1;3;5'.
                        Если вопрос относится к одному модулю, то введите просто номер этого модуля\s
                        Чтобы посмотреть нумерацию модулей введите команду '/get_modules'.""";
    public static final String AddModuleMessage = "Введите название модуля.\n" +
            "Чтобы посмотреть уже существующие модули введите '/get_modules'.";
    public static final String AddTopicMessage = """
                        Введите название темы и номер соответсвтующего ей модуля.
                        Пример:
                        Алканы;2
                        Здесь 'Алканы' - название темы, а '2' - номер модуля 'Органика', который соответствует теме'Алканы'.
                        Узнать номера модулей можно с помощью команды '/get_modules'.""";
    public static final String FormatMessage = """
                    Вводить ответы на вопрос в формате:\s
                    'Текст первого варианта ответа';Верный;
                    'Текст второго варианта ответа';Неверный;
                    'Текст третьего варианта ответа';Неверный;...\s
                    И так сколько нужно вариантов ответа.""";
    public static final String EnteringModuleMessage = """
                        Введите номера тем, к которым относится данный вопрос.
                        Пример оформления: '4;5'
                        Если вопрос относится к одной теме, введите просто число
                        Чтобы посмотреть нумерацию тем введите команду '/get_topics'.""";
    public static final String EnteringTopicMessage = "Введите вопрос.";
    public static final String EnteringQuestionMessage = "Введите ответы на вопрос. Посмотреть формат ввода '/format'.";
    public static final String EnteringAnswersMessage ="Вопрос добавлен в базу данных.";
    public static final String AddingModuleMessage = "Модуль добавлен в базу данных.";
    public static final String AddingTopicMessage = "Тема добавлена в базу данных.";

    public static final String PressingModuleForGettingTest = "Выберите модуль, из которого вы хотите взять тест.";
    public static final String PressingTopicForGettingTest = "Выберите тему, из которой вы хотите взять тест.";
}

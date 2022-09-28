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
    public static final String AnswerWasAdded = "Вопрос добавлен в базу данных.";
    public static final String AddingModuleMessage = "Модуль добавлен в базу данных.";
    public static final String AddingTopicMessage = "Тема добавлена в базу данных.";

    public static final String PressingModuleForGettingTest = "Выберите модуль, из которого вы хотите взять тест.";
    public static final String PressingTopicForGettingTest = "Выберите тему, из которой вы хотите взять тест.";

    public static final String AreYouAdmin = "Являетесь ли вы преподавателем?";
    public static final String YouDoNotHaveAccessToAdminCommands = "Вы не имеете доступа к командам администратора. " +
            "Чтобы получить доступ введите '/enter_key_code'.";
    public static final String YouAreAlreadyAdmin = "Вы уже имеете доступ к командам администратора.";
    public static final String YouAreNotRegistered = "Вы не зарегистрированы. " +
            "Введите '/register' чтобы зарегистрироваться.";
    public static final String EnterPassword = "Введите пароль.";
    public static final String AnswerUpperQuestion = "Ответьте на вопрос выше.";
    public static final String SuccessfulRegistration = "Успешная регистрация.";
    public static final String WrongKeyCode = "Ключ доступа неверный. Регистрация не прошла.";
    public static final String CorrectKeyCode = "Код верный. Теперь вы имеете доступ к командам администратора.";
    public static final String WrongKeyCodeAgterRegistration = "Вы ввели неправильный код.";
    public static final String EnterKeyCode = "Введите ключ доступа.";
    public static final String RegistrationError = "Ошибка регистрации";
    public static final String CollectedBalls = "Набранные баллы: ";
    public static final String CollectedForTestBalls = "Набранные баллы за тест по теме";
    public static final String Yes = "Да";
    public static final String No = "Нет";
    public static final String UserIsAlreadyRegistered = "Пользователь уже зарегестрирован.";
    public static final String EnterYourName = "Введите ваше имя.";
    public static final String EnterMaxBalls = "Введите количество баллов, которое будет даваться за правильный ответ.";
}

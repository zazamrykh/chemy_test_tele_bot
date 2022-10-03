import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.glassfish.grizzly.utils.Pair;

public class DataBaseHandler extends DataBaseConfig {
    Connection dbConnection;

    public Connection getDbConnection() throws ClassNotFoundException, SQLException {
        String connectionString = "jdbc:mysql://" + dbHost + ":"
                + dbPort + "/" + dbName + "?" +
                "autoReconnect=true&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift" +
                "=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        Class.forName("com.mysql.cj.jdbc.Driver");

        dbConnection = DriverManager.getConnection(connectionString, dbUser, dbPassword);
        return dbConnection;
    }

    public int getRandomQuestionId() {
        String selectRandomQuestion = "SELECT " + DBConsts.QUESTION_ID +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE +
                " ORDER BY RAND() LIMIT 1";

        Statement statement = null;
        try {
            statement = getDbConnection().createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        ResultSet resultSet;
        try {
            assert statement != null;
            resultSet = statement.executeQuery(selectRandomQuestion);
            if (resultSet.next()) {
                return Integer.parseInt(resultSet.getString(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String getQuestion(int questionId) {
        String selectRandomQuestion = "SELECT " + DBConsts.QUESTION_TEXT +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE +
                " WHERE " + DBConsts.QUESTION_ID + " = " + questionId;

        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            assert statement != null;
            resultSet = statement.executeQuery(selectRandomQuestion);
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<Integer, Pair<String, Boolean>> getAnswersAtQuestion(int questionId) {
        String selectAnswersAtQuestionQuery = "SELECT " + DBConsts.ANSWER_ID + ", "
                + DBConsts.ANSWER_TEXT + ", "
                + DBConsts.IS_CORRECT +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.ANSWER_TABLE +
                " WHERE " + DBConsts.QUESTION_ID + " = " + questionId;

        HashMap<Integer, Pair<String, Boolean>> answers = new HashMap<>();

        Statement statement = null;
        try {
            statement = getDbConnection().createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        ResultSet resultSet;
        try {
            assert statement != null;
            resultSet = statement.executeQuery(selectAnswersAtQuestionQuery);
            while (resultSet.next()) {
                if (Objects.equals(resultSet.getString(3), "true")) {
                    answers.put(Integer.valueOf(resultSet.getString(1)),
                            new Pair<>(resultSet.getString(2), true));
                } else {
                    answers.put(Integer.valueOf(resultSet.getString(1)),
                            new Pair<>(resultSet.getString(2), false));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    public List<Pair<Integer, String>> getTopics() {
        String selectTopicsQuery = "SELECT " + DBConsts.TOPIC_ID + ", " + DBConsts.TOPIC_NAME +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.TOPIC_TABLE +
                " ORDER BY " + DBConsts.MODULE_ID;

        Statement statement;
        List<Pair<Integer, String>> topics = new ArrayList<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectTopicsQuery);
            while (resultSet.next()) {
                topics.add(new Pair<>(Integer.valueOf(resultSet.getString(1)),
                        resultSet.getString(2)));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return topics;
    }

    public HashMap<Integer, String> getTopics(Integer moduleId) {
        String selectTopicsQuery = "SELECT " + DBConsts.TOPIC_ID + ", " + DBConsts.TOPIC_NAME +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.TOPIC_TABLE +
                " WHERE " + DBConsts.MODULE_ID + " = " + moduleId;

        Statement statement;
        HashMap<Integer, String> topics = new HashMap<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectTopicsQuery);
            while (resultSet.next()) {
                topics.put(Integer.valueOf(resultSet.getString(1)), resultSet.getString(2));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return topics;
    }

    public HashMap<Integer, String> getModules() {
        String selectModulesQuery = "SELECT " + DBConsts.MODULE_ID + ", " + DBConsts.MODULE_NAME +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.MODULE_TABLE;
        Statement statement;
        HashMap<Integer, String> modules = new HashMap<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectModulesQuery);
            while (resultSet.next()) {
                modules.put(Integer.valueOf(resultSet.getString(1)), resultSet.getString(2));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return modules;
    }

    public Pair<Integer, String> getModule(Integer topicId) {
        // Returns Pair<Integer, String> where key is moduleId, value is module name using topicId
        String selectModuleIdAndNameUsingTopicId = "SELECT " + DBConsts.MODULE_ID + ", " + DBConsts.MODULE_NAME +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.MODULE_TABLE +
                " WHERE " + DBConsts.MODULE_ID + " = " +
                " ( SELECT " + DBConsts.MODULE_ID + " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.TOPIC_TABLE +
                " WHERE " + DBConsts.TOPIC_ID + " = " + topicId.toString() + ")";


        Statement statement;
        Pair<Integer, String> module = new Pair<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectModuleIdAndNameUsingTopicId);
            while (resultSet.next()) {
                module.setFirst(Integer.valueOf(resultSet.getString(1)));
                module.setSecond(resultSet.getString(2));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return module;
    }

    public List<Question> getQuestions(int topicId) {
        String selectQuestionIdAndText = "SELECT DISTINCT " + DBConsts.QUESTION_ID + ", " + DBConsts.QUESTION_TEXT +
                ", " + DBConsts.MAX_BALL +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE +
                " INNER JOIN " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TOPIC_MODULE_TABLE +
                " USING (" + DBConsts.QUESTION_ID + ") WHERE " + DBConsts.TOPIC_ID + " = " + topicId;

        Statement statement;
        HashMap<Integer, Pair<String, Integer>> questionIdTextAndBall = new HashMap<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectQuestionIdAndText);
            while (resultSet.next()) {
                questionIdTextAndBall.put(Integer.valueOf(resultSet.getString(1)),
                        new Pair<>(resultSet.getString(2),
                                Integer.valueOf(resultSet.getString(3))));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        HashMap<Integer, Pair<String, Boolean>> answers;
        List<Question> questions = new ArrayList<>();
        for (Map.Entry<Integer, Pair<String, Integer>> entry : questionIdTextAndBall.entrySet()) {
            answers = getAnswersAtQuestion(entry.getKey());
            questions.add(new Question(entry.getKey(), topicId, entry.getValue().getFirst(),
                    answers, entry.getValue().getSecond()));
        }
        return questions;
    }

    public String getTopicName(int topicId) {
        String selectTopicsQuery = "SELECT " + DBConsts.TOPIC_NAME +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.TOPIC_TABLE +
                " WHERE " + DBConsts.TOPIC_ID + " = " + topicId;

        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectTopicsQuery);
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addQuestionWithAnswers(String[] moduleIds, String[] topicIds, String questionText, String testNumber,
                                       HashMap<String, Pair<Boolean, Boolean>> textIsCorrectIsHandwrittenAnswer,
                                       int maxBall) {
        String questionId = String.valueOf(addQuestion(moduleIds, topicIds, questionText, testNumber, maxBall));
        addAnswers(questionId, textIsCorrectIsHandwrittenAnswer);
    }

    //  Нужно все add сделать boolean, чтобы было понятно, выполнился запрос или нет
    public int addQuestion(String[] moduleIds, String[] topicIds,
                           String questionText, String testNumber, int maxBall) {
        // Add question to database and return question_id of inserted question
        String insertQuestionQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE +
                " (" + DBConsts.TEST_NUMBER + ", " + DBConsts.QUESTION_TEXT + ", " + DBConsts.MAX_BALL + ") " +
                "VALUES (?, ?, ?)";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insertQuestionQuery);
            prSt.setString(1, testNumber);
            prSt.setString(2, questionText);
            prSt.setString(3, String.valueOf(maxBall));
            prSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        int questionId = -1;
        String getIdQuestionQuery = "SELECT " + DBConsts.QUESTION_ID +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE +
                " WHERE " + DBConsts.QUESTION_TEXT + " = " + "'" + questionText + "'";
        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            assert statement != null;
            resultSet = statement.executeQuery(getIdQuestionQuery);
            if (resultSet.next()) {
                questionId = Integer.parseInt(resultSet.getString(1));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        for (String moduleId : moduleIds) {
            for (String topicId : topicIds) {
                String insertQuestionTopicModuleQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." +
                        DBConsts.QUESTION_TOPIC_MODULE_TABLE +
                        " (" + DBConsts.QUESTION_ID + ", " + DBConsts.TOPIC_ID + ", " + DBConsts.MODULE_ID +
                        ") VALUES (?, ?, ?)";
                try {
                    PreparedStatement prSt = getDbConnection().prepareStatement(insertQuestionTopicModuleQuery);
                    prSt.setString(1, String.valueOf(questionId));
                    prSt.setString(2, topicId);
                    prSt.setString(3, moduleId);
                    prSt.executeUpdate();
                } catch (SQLException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return questionId;
    }

    public void addAnswers(String questionId,
                           HashMap<String, Pair<Boolean, Boolean>> textIsCorrectIsHandwrittenAnswer) {
        String insertAnswerQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.ANSWER_TABLE +
                " (" + DBConsts.QUESTION_ID + ", " + DBConsts.IS_CORRECT + ", " +
                DBConsts.IS_HANDWRITTEN + ", " + DBConsts.ANSWER_TEXT + ") " +
                " VALUES (?, ?, ?, ?)";

        for (Map.Entry<String, Pair<Boolean, Boolean>> entry : textIsCorrectIsHandwrittenAnswer.entrySet()) {
            String answerText = entry.getKey();
            String isCorrect = String.valueOf(entry.getValue().getFirst());
            String isHandwritten = String.valueOf(entry.getValue().getSecond());

            try {
                PreparedStatement prSt = getDbConnection().prepareStatement(insertAnswerQuery);
                prSt.setString(1, questionId);
                prSt.setString(2, isCorrect);
                prSt.setString(3, isHandwritten);
                prSt.setString(4, answerText);
                prSt.executeUpdate();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void addModule(String moduleName) {
        String insertQuestionQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.MODULE_TABLE +
                " (" + DBConsts.MODULE_NAME + ") VALUES (?)";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insertQuestionQuery);
            prSt.setString(1, moduleName);
            prSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addTopic(String topicName, int moduleId) {
        String insertQuestionQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.TOPIC_TABLE +
                " (" + DBConsts.MODULE_ID + ", " + DBConsts.TOPIC_NAME + ") VALUES (?, ?)";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insertQuestionQuery);
            prSt.setString(1, String.valueOf(moduleId));
            prSt.setString(2, topicName);
            prSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean isUserRegistered(String studentId) {
        String checkUserRegisteredQuery = "SELECT " + DBConsts.STUDENT_ID +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.STUDENT_TABLE +
                " WHERE " + DBConsts.STUDENT_ID + " = " + studentId;
        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(checkUserRegisteredQuery);
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addStudent(long chatId, String studentName,
                              boolean isAdmin) {
        String insertStudentQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.STUDENT_TABLE +
                " (" + DBConsts.STUDENT_ID + ", " + DBConsts.STUDENT_NAME + ", "
                + DBConsts.IS_ADMIN + ", " + DBConsts.DISCIPLINE_ID
                + ") VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insertStudentQuery);
            prSt.setString(1, String.valueOf(chatId));
            prSt.setString(2, studentName);
            prSt.setString(3, String.valueOf(isAdmin));
            prSt.setString(4, "1");
            prSt.executeUpdate();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getStudentId(String login) {
        String getStudentIdQuery = "SELECT " + DBConsts.STUDENT_ID +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.STUDENT_TABLE +
                " WHERE " + DBConsts.STUDENT_NAME + " = " + "\"" + login + "\"";

        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(getStudentIdQuery);
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteStudent(long studentId) {
        String deleteStudentQuery = "DELETE FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.STUDENT_TABLE +
                " WHERE " + DBConsts.STUDENT_ID + " = " + studentId;
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(deleteStudentQuery);
            prSt.executeUpdate();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkAccessKey(String accessKey) {
        try {
            Integer.parseInt(accessKey);
        } catch (NumberFormatException e) {
            return false;
        }
        String checkAccessKeyQuery = "SELECT " + DBConsts.DISCIPLINE_NAME +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.DISCIPLINE +
                " WHERE " + DBConsts.KEY_CODE + " = " + accessKey;

        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(checkAccessKeyQuery);
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean tryMakeAdmin(long chatId, String keyCode) {
        if (!checkAccessKey(keyCode)) {
            return false;
        }
        String setIsAdminQuery = "UPDATE " + DBConsts.SCHEMA_NAME + "." + DBConsts.STUDENT_TABLE +
                " SET " + DBConsts.IS_ADMIN + " = " + "'true'" +
                " WHERE " + DBConsts.STUDENT_ID + " = " + chatId;
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(setIsAdminQuery);
            prSt.executeUpdate();
            return true;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkIsAdmin(long chatId) {
        String checkIsAdminQuery = "SELECT " + DBConsts.IS_ADMIN +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.STUDENT_TABLE +
                " WHERE " + DBConsts.STUDENT_ID + " = " + chatId;

        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(checkIsAdminQuery);
            if (resultSet.next()) {
                return resultSet.getString(1).equals("true");
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addStudentAnswer(User currentUser) {
        Testing testing = currentUser.getTesting();
        UserAnswer userAnswer = testing.getUserAnswer(testing.getUserAnswerNumber() - 1);
        for (Integer userAnswerId : userAnswer.getUserAnswerIds()) {
            String insertStudentAnswerQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.STUDENT_ANSWER_TABLE +
                    " (" + DBConsts.STUDENT_ID + ", " + DBConsts.QUESTION_ID + ", "
                    + DBConsts.ANSWER_ID + ", " + DBConsts.IS_CORRECT + ", " + DBConsts.BEGINNING_DATETIME
                    + ", " + DBConsts.END_DATETIME + ", " + DBConsts.BALLS + ", " + DBConsts.TESTING_ID
                    + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try {
                PreparedStatement prSt = getDbConnection().prepareStatement(insertStudentAnswerQuery);
                prSt.setString(1, String.valueOf(currentUser.getChatId()));
                prSt.setString(2, String.valueOf(userAnswer.getQuestionId()));
                prSt.setString(3, String.valueOf(userAnswerId));
                prSt.setString(4, String.valueOf(userAnswer.isFullyCorrect()));
                prSt.setString(5, userAnswer.getBeginningDateTime());
                prSt.setString(6, userAnswer.getEndDateTime());
                prSt.setString(7, String.valueOf(testing.getCurrentQuestion().maxBall()));
                prSt.setString(8, String.valueOf(testing.getTestingId()));
                prSt.executeUpdate();
            } catch (SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void addTesting(Testing testing) {
        String addTestingQuery = "INSERT INTO " + DBConsts.SCHEMA_NAME + "." + DBConsts.TESTING_TABLE +
                " (" + DBConsts.STUDENT_ID + ", " + DBConsts.BEGINNING_DATETIME + ", " + DBConsts.TOPIC_ID
                + ", " +  DBConsts.MAX_BALL + ") VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(addTestingQuery);
            prSt.setString(1, String.valueOf(testing.getStudentId()));
            prSt.setString(2, testing.getBeginningDateTime());
            prSt.setString(3, String.valueOf(testing.getTopicId()));
            prSt.setString(4, String.valueOf(getMaxBallsForTopic(testing.getTopicId())));
            prSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        String selectTestingIdQuery = "SELECT MAX(" + DBConsts.TESTING_ID + ")" +
                " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.TESTING_TABLE;
        int testingId = 0;
        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectTestingIdQuery);
            if (resultSet.next()) {
                testingId = Integer.parseInt(resultSet.getString(1));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        testing.setTestingId(testingId);
    }

    public void completeTesting(Testing testing) {
        String addTestingQuery = "UPDATE " + DBConsts.SCHEMA_NAME + "." + DBConsts.TESTING_TABLE +
                " SET " + DBConsts.END_DATETIME + " = " + "'" + testing.getEndDateTime() + "'" +
                ", " + DBConsts.RESULT + " = " + testing.getPoints() +
                " WHERE " + DBConsts.TESTING_ID + " = " + testing.getTestingId();
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(addTestingQuery);
            prSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getMaxBallsForTopic (int topicId){
        String queryIn = "SELECT DISTINCT " + DBConsts.QUESTION_ID + ", "
                + DBConsts.TOPIC_ID + ", " + DBConsts.MAX_BALL
                + " FROM " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TOPIC_MODULE_TABLE
                + " INNER JOIN " + DBConsts.SCHEMA_NAME + "." + DBConsts.QUESTION_TABLE
                + " USING (" + DBConsts.QUESTION_ID + ")"
                + " WHERE " + DBConsts.TOPIC_ID + " = " + topicId;
        String getMaxBallsQuery = "SELECT SUM(" + DBConsts.MAX_BALL + ")" +
                " FROM (" + queryIn + ") query_in";

        Statement statement;
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(getMaxBallsQuery);
            if (resultSet.next()) {
                return Integer.parseInt(resultSet.getString(1));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }
}

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        String selectRandomQuestion = "SELECT " + DBConstants.QUESTION_ID +
                " FROM " + DBConstants.SCHEMA_NAME + "." + DBConstants.QUESTION_TABLE +
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
        String selectRandomQuestion = "SELECT " + DBConstants.QUESTION_TEXT +
                " FROM " + DBConstants.SCHEMA_NAME + "." + DBConstants.QUESTION_TABLE +
                " WHERE " + DBConstants.QUESTION_ID + " = " + questionId;

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

    public void addQuestion(String question_text) {
        String insertQuestion = "INSERT INTO " + DBConstants.SCHEMA_NAME + "." + DBConstants.QUESTION_TABLE +
                " (" + DBConstants.QUESTION_TEXT + ") VALUES (?)";
        try {
            PreparedStatement prSt = getDbConnection().prepareStatement(insertQuestion);
            prSt.setString(1, question_text);
            prSt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<String> getAllQuestions() {
        String selectAllQuestion = "SELECT " + DBConstants.QUESTION_ID + ", " + DBConstants.QUESTION_TEXT +
                " FROM " + DBConstants.SCHEMA_NAME + "." + DBConstants.QUESTION_TABLE;

        Statement statement;
        List<String> allQuestions = new ArrayList<>();
        List<String> questionId = new ArrayList<>();
        try {
            statement = getDbConnection().createStatement();
            ResultSet resultSet;
            resultSet = statement.executeQuery(selectAllQuestion);
            while (resultSet.next()) {
                questionId.add(resultSet.getString(1));
                allQuestions.add(resultSet.getString(2));
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return allQuestions;
    }

    public HashMap<Integer, Pair<String, Boolean>> getAnswersAtQuestion(int questionId) {
        String selectAnswersAtQuestion = "SELECT " + DBConstants.ANSWER_ID + ", "
                + DBConstants.ANSWER_TEXT + ", "
                + DBConstants.IS_CORRECT +
                " FROM " + DBConstants.SCHEMA_NAME + "." + DBConstants.ANSWER_TABLE +
                " WHERE " + DBConstants.QUESTION_ID + " = " + questionId;

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
            resultSet = statement.executeQuery(selectAnswersAtQuestion);
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
}

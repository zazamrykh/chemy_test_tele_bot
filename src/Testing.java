import java.util.HashMap;
import java.util.List;

public class Testing {
    private final long studentId;
    private final String beginningDateTime;
    private String endDateTime;
    private int points;
    private final int topicId;
    private final String topicName;
    private final List<Question> questions;
    private int idCurrentQuestion;
    private final HashMap<Integer, UserAnswer> userAnswers = new HashMap<>();
    private Integer userAnswerNumber = 0;

    Testing(long studentId, String beginningDateTime, int topicId, String topicName, List<Question> questions) {
        this.topicName = topicName;
        this.studentId = studentId;
        this.beginningDateTime = beginningDateTime;
        this.topicId = topicId;
        this.questions = questions;
    }

    public long getStudentId() {
        return studentId;
    }

    public String getBeginningDateTime() {
        return beginningDateTime;
    }

    public int getTopicId() {
        return topicId;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void incrementPoints() {
        points++;
    }

    public Question getCurrentQuestion() {
        return questions.get(idCurrentQuestion);
    }

    public void incrementIdCurrentQuestion() {
        idCurrentQuestion++;
    }

    public boolean isLastQuestion() {
        return idCurrentQuestion == questions.size() - 1;
    }

    public void addUserAnswer(int questionId, String beginningDateTime, DataBaseHandler dbHandler) {
        UserAnswer userAnswer = new UserAnswer(questionId, dbHandler);
        userAnswer.setBeginningDateTime(beginningDateTime);
        userAnswers.put(userAnswerNumber, userAnswer);
        userAnswerNumber++;
    }

    public UserAnswer getUserAnswer(Integer userAnswerNumber) {
        return userAnswers.get(userAnswerNumber);
    }

    public Integer getUserAnswerNumber() {
        return userAnswerNumber;
    }

    public String getTopicName() {
        return topicName;
    }
}

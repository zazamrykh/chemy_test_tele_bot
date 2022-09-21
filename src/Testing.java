import java.util.List;

public class Testing {
    private final long studentId;
    private final String beginningDateTime;
    private String endDateTime;
    private int points;
    private final int topicId;
    private List <Question> questions;
    private int idCurrentQuestion;

    Testing(long studentId, String beginningDateTime, int topicId){
        this.studentId = studentId;
        this.beginningDateTime = beginningDateTime;
        this.topicId = topicId;
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

    public void setQuestions(List<Question> questions) {
        this.questions =questions;
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
}

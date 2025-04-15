package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static edu.francis.my.sfupa.HelloFXApplication.springContext;

@Component
public class InstructorEvalAnalysisController {

    @FXML private ComboBox<String> courseCmb;
    @FXML private ComboBox<String> yearCmb;
    @FXML private ComboBox<String> semesterCmb;
    @FXML private VBox analysisContainer;
    @FXML private TextArea openEndedAnalysisArea;
    @FXML private BarChart<String, Number> likertBarChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @Autowired private CourseRepository courseRepository;
    @Autowired private SchoolYearRepository schoolYearRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private ResponseLikertRepository responseLikertRepository;
    @Autowired private ResponseOpenRepository responseOpenRepository;
    @Autowired private QuestionsRepository questionsRepository;
    @Autowired private CourseEvalRepository courseEvalRepository;

    @FXML
    public void initialize() {
        setupCourseComboBox();
        setupYearComboBox();
        setupSemesterComboBox();
        setupChartStyle();
    }

    private void setupCourseComboBox() {
        if (courseCmb != null) {
            List<Course> courses = new ArrayList<>();
            courseRepository.findAll().forEach(courses::add);
            List<String> courseCodes = courses.stream()
                    .map(Course::getcourseCode)
                    .collect(Collectors.toList());
            courseCmb.setItems(FXCollections.observableArrayList(courseCodes));
        }
    }

    private void setupChartStyle() {
        if (likertBarChart != null) {
            likertBarChart.setStyle("-fx-background-color: white;");
            likertBarChart.setTitle("Likert Scale Responses");
            
            if (xAxis != null) {
                xAxis.setStyle("-fx-font-size: 12px;");
                xAxis.setTickLabelRotation(-45);
            }
            
            if (yAxis != null) {
                yAxis.setStyle("-fx-font-size: 12px;");
            }
        }
    }

    @FXML
    public void analyzeEvaluations() {
        if (courseCmb.getValue() == null ||
                yearCmb.getValue() == null ||
                semesterCmb.getValue() == null) {
            showAlert("Please select Course, Year, and Semester");
            return;
        }

        analyzeLikertResponses();
        analyzeOpenEndedResponses();
    }

    private void analyzeLikertResponses() {
        likertBarChart.getData().clear();
        Map<Questions, List<ResponseLikert>> responsesGrouped = new HashMap<>();

        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        for (CourseEval eval : allEvals) {
            if (eval.getCourse().getClassCode().getcourseCode().equals(courseCmb.getValue())) {
                for (ResponseLikert response : responseLikertRepository.findAll()) {
                    if (response.getCourseEval().getId().equals(eval.getId())) {
                        responsesGrouped
                            .computeIfAbsent(response.getQuestion(), k -> new ArrayList<>())
                            .add(response);
                    }
                }
            }
        }

        for (Map.Entry<Questions, List<ResponseLikert>> entry : responsesGrouped.entrySet()) {
            double avgResponse = calculateAverageResponse(entry.getValue());
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(entry.getKey().getText());
            series.getData().add(new XYChart.Data<>("Average", avgResponse));
            likertBarChart.getData().add(series);
        }
    }

    private double calculateAverageResponse(List<ResponseLikert> responses) {
        if (responses.isEmpty()) return 0.0;
        double sum = responses.stream()
                .mapToDouble(response -> mapLikertToNumeric(response.getResponse()))
                .sum();
        return sum / responses.size();
    }

    private void analyzeOpenEndedResponses() {
        StringBuilder analysis = new StringBuilder();
        analysis.append("Open-Ended Response Analysis\n\n");

        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        List<ResponseOpen> relevantResponses = new ArrayList<>();

        for (CourseEval eval : allEvals) {
            if (eval.getCourse().getClassCode().getcourseCode().equals(courseCmb.getValue())) {
                for (ResponseOpen response : responseOpenRepository.findAll()) {
                    if (response.getCourseEval().getId().equals(eval.getId())) {
                        relevantResponses.add(response);
                    }
                }
            }
        }

        if (!relevantResponses.isEmpty()) {
            String aggregatedResponses = relevantResponses.stream()
                    .map(ResponseOpen::getResponse)
                    .collect(Collectors.joining("\n\n"));
            analysis.append(performBasicTextAnalysis(aggregatedResponses));
        } else {
            analysis.append("No open-ended responses found for the selected criteria.");
        }

        openEndedAnalysisArea.setText(analysis.toString());
    }

    private double mapLikertToNumeric(String likertResponse) {
        switch (likertResponse.toLowerCase()) {
            case "strongly disagree": return 1.0;
            case "disagree": return 2.0;
            case "neutral": return 3.0;
            case "agree": return 4.0;
            case "strongly agree": return 5.0;
            default: return 0.0;
        }
    }

    private String performBasicTextAnalysis(String aggregatedResponses) {
        String[] sentences = aggregatedResponses.split("[.!?]");
        Map<String, Long> wordFrequency = Arrays.stream(sentences)
                .flatMap(sentence -> Arrays.stream(sentence.toLowerCase().split("\\s+")))
                .filter(word -> word.length() > 3)
                .collect(Collectors.groupingBy(word -> word, Collectors.counting()));

        List<Map.Entry<String, Long>> topWords = wordFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toList());

        StringBuilder analysis = new StringBuilder("Open-Ended Response Analysis:\n\n");
        analysis.append("Top Recurring Themes:\n");
        topWords.forEach(entry ->
                analysis.append(entry.getKey())
                        .append(": ")
                        .append(entry.getValue())
                        .append(" occurrences\n")
        );

        return analysis.toString();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Evaluation Analysis");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    public void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About SFU PA");
        alert.setHeaderText("SFU PA Application");
        alert.setContentText("This application is designed to manage gradebooks, instructor evaluations, and guest lecturers.");
        alert.showAndWait();
    }

    @FXML
    public void handleGuestLecturer(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "CourseSurvey.fxml", springContext);
    }

    @FXML
    public void handleGradebook(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "Gradebook.fxml", springContext);
    }

    @FXML
    public void handleInstructorEval(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEval.fxml", springContext);
    }

    @FXML
    public void handleBackMain(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "main-view.fxml", springContext);
    }

    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEval.fxml", springContext);
    }

    private void setupYearComboBox() {
        if (yearCmb != null) {
            List<SchoolYear> schoolYears = (List<SchoolYear>) schoolYearRepository.findAll();
            List<String> schoolYearNames = schoolYears.stream()
                    .map(SchoolYear::getName)
                    .collect(Collectors.toList());
            yearCmb.setItems(FXCollections.observableArrayList(schoolYearNames));
        }
    }

    private void setupSemesterComboBox() {
        if (semesterCmb != null) {
            semesterCmb.setItems(FXCollections.observableArrayList("Spring", "Summer", "Fall", "Winter"));
        }
    }
}

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
import org.springframework.context.ApplicationContext;

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
    @FXML private ComboBox<String> lecturerCombo;

    @Autowired private CourseRepository courseRepository;
    @Autowired private SchoolYearRepository schoolYearRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private ResponseLikertRepository responseLikertRepository;
    @Autowired private ResponseOpenRepository responseOpenRepository;
    @Autowired private QuestionsRepository questionsRepository;
    @Autowired private LecturerRepository lecturerRepository;
    @Autowired private CourseEvalRepository courseEvalRepository;


    /*
    @FXML
    public void initialize() {
        // Convert Iterable<Course> to List<Course> and then set it to the ComboBox
        List<Course> courses = new ArrayList<>();
        courseRepository.findAll().forEach(courses::add);
        courseCmb.setItems(FXCollections.observableArrayList(courses));

        // Set items for yearCmb (ComboBox for SchoolYear)
        List<SchoolYear> schoolYears = (List<SchoolYear>) schoolYearRepository.findAll(); // Properly cast Iterable to List
        yearCmb.setItems(FXCollections.observableArrayList(schoolYears));

        // Set items for semesterCmb (ComboBox for Semester)
        List<Semester> semesters = (List<Semester>) semesterRepository.findAll(); // Properly cast Iterable to List
        semesterCmb.setItems(FXCollections.observableArrayList(semesters));

        // Set labels for chart axes
        xAxis.setLabel("Question");
        yAxis.setLabel("Average Response");
    }



     */

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
    public void initialize() {
        setupLecturerComboBox();
        setupYearComboBox();
        setupSemesterComboBox();
        setupChartStyle();
    }

    private void setupLecturerComboBox() {
        Iterable<Lecturer> lecturers = lecturerRepository.findAll();
        List<String> lecturerNames = new ArrayList<>();
        for (Lecturer lecturer : lecturers) {
            lecturerNames.add(lecturer.getFName() + " " + lecturer.getLName());
        }
        lecturerCombo.setItems(FXCollections.observableArrayList(lecturerNames));
        
        lecturerCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateCourseComboBox(newVal);
            }
        });
    }

    private void updateCourseComboBox(String lecturerName) {
        String[] nameParts = lecturerName.split(" ");
        if (nameParts.length < 2) return;

        String firstName = nameParts[0];
        String lastName = nameParts[1];

        // Find the lecturer
        Lecturer lecturer = findLecturerByName(firstName, lastName);
        if (lecturer == null) return;

        // Get all course evaluations for this lecturer
        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        Set<String> uniqueCourses = new HashSet<>();
        
        for (CourseEval eval : allEvals) {
            if (eval.getLecturer().getFName().equals(lecturer.getFName()) && 
                eval.getLecturer().getLName().equals(lecturer.getLName())) {
                uniqueCourses.add(eval.getCourse().getClassCode().getcourseCode());
            }
        }

        courseCmb.setItems(FXCollections.observableArrayList(uniqueCourses));
    }

    private Lecturer findLecturerByName(String firstName, String lastName) {
        Iterable<Lecturer> lecturers = lecturerRepository.findAll();
        for (Lecturer lecturer : lecturers) {
            if (lecturer.getFName().equals(firstName) && lecturer.getLName().equals(lastName)) {
                return lecturer;
            }
        }
        return null;
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
        if (lecturerCombo.getValue() == null || courseCmb.getValue() == null ||
                yearCmb.getValue() == null || semesterCmb.getValue() == null) {
            showAlert("Please select Lecturer, Course, Year, and Semester");
            return;
        }

        String[] nameParts = lecturerCombo.getValue().split(" ");
        Lecturer lecturer = findLecturerByName(nameParts[0], nameParts[1]);
        if (lecturer == null) return;

        analyzeLikertResponses(lecturer);
        analyzeOpenEndedResponses(lecturer);
    }

    private void analyzeLikertResponses(Lecturer lecturer) {
        likertBarChart.getData().clear();
        Map<Questions, List<ResponseLikert>> responsesGrouped = new HashMap<>();

        // Get course evaluations for the selected lecturer and course
        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        for (CourseEval eval : allEvals) {
            if (eval.getLecturer().getFName().equals(lecturer.getFName()) && 
                eval.getLecturer().getLName().equals(lecturer.getLName()) &&
                eval.getCourse().getClassCode().getcourseCode().equals(courseCmb.getValue())) {
                
                // Get all Likert responses for this evaluation
                for (ResponseLikert response : responseLikertRepository.findAll()) {
                    if (response.getCourseEval().getId().equals(eval.getId())) {
                        responsesGrouped
                            .computeIfAbsent(response.getQuestion(), k -> new ArrayList<>())
                            .add(response);
                    }
                }
            }
        }

        // Create chart data
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

    private void analyzeOpenEndedResponses(Lecturer lecturer) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("Open-Ended Response Analysis for ")
                .append(lecturer.getFName())
                .append(" ")
                .append(lecturer.getLName())
                .append("\n\n");

        // Get course evaluations for the selected lecturer and course
        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        List<ResponseOpen> relevantResponses = new ArrayList<>();

        for (CourseEval eval : allEvals) {
            if (eval.getLecturer().getFName().equals(lecturer.getFName()) && 
                eval.getLecturer().getLName().equals(lecturer.getLName()) &&
                eval.getCourse().getClassCode().getcourseCode().equals(courseCmb.getValue())) {
                
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

    // --- Sidebar Navigation ---
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

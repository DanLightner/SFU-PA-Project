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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class GuestLecturerAnalysisController {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ResponseLikertRepository responseLikertRepository;

    @Autowired
    private ResponseOpenRepository responseOpenRepository;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @FXML
    private ComboBox<String> lecturerCombo;

    @FXML
    private ComboBox<String> courseCombo;

    @FXML
    private ComboBox<String> yearCombo;

    @FXML
    private ComboBox<String> semesterCombo;

    @FXML
    private BarChart<String, Number> likertBarChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private TextArea openEndedAnalysisArea;

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
        if (lecturerName == null || lecturerName.trim().isEmpty()) {
            courseCombo.getItems().clear();
            return;
        }

        String[] nameParts = lecturerName.split(" ", 2);
        if (nameParts.length < 2) {
            courseCombo.getItems().clear();
            return;
        }

        String firstName = nameParts[0].trim();
        String lastName = nameParts[1].trim();

        // Find the lecturer
        Lecturer lecturer = findLecturerByName(firstName, lastName);
        if (lecturer == null) {
            courseCombo.getItems().clear();
            return;
        }

        // Get all course evaluations for this lecturer
        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        Set<String> uniqueCourses = new HashSet<>();
        
        for (CourseEval eval : allEvals) {
            if (eval.getLecturer() != null && 
                eval.getLecturer().getFName().equals(lecturer.getFName()) && 
                eval.getLecturer().getLName().equals(lecturer.getLName()) &&
                eval.getCourse() != null &&
                eval.getCourse().getClassCode() != null) {
                    
                String courseCode = eval.getCourse().getClassCode().getcourseCode();
                String courseName = eval.getCourse().getClassCode().getName();
                if (courseCode != null && courseName != null) {
                    uniqueCourses.add(courseCode + " - " + courseName);
                }
            }
        }

        List<String> sortedCourses = new ArrayList<>(uniqueCourses);
        Collections.sort(sortedCourses);
        courseCombo.setItems(FXCollections.observableArrayList(sortedCourses));
    }

    private void setupYearComboBox() {
        if (yearCombo != null) {
            List<SchoolYear> schoolYears = (List<SchoolYear>) schoolYearRepository.findAll();
            List<String> schoolYearNames = schoolYears.stream()
                    .map(SchoolYear::getName)
                    .collect(Collectors.toList());
            yearCombo.setItems(FXCollections.observableArrayList(schoolYearNames));
        }
    }

    private void setupSemesterComboBox() {
        if (semesterCombo != null) {
            semesterCombo.setItems(FXCollections.observableArrayList("Spring", "Summer", "Fall", "Winter"));
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
    public void analyzeData() {
        if (lecturerCombo.getValue() == null || courseCombo.getValue() == null ||
                yearCombo.getValue() == null || semesterCombo.getValue() == null) {
            showAlert("Please select all fields before analyzing data.");
            return;
        }

        String[] lecturerName = lecturerCombo.getValue().split(" ");
        String courseCode = courseCombo.getValue().split(" - ")[0];
        String year = yearCombo.getValue();
        String semester = semesterCombo.getValue();

        // Find the lecturer
        Lecturer lecturer = findLecturerByName(lecturerName[0], lecturerName[1]);
        if (lecturer == null) {
            showAlert("Lecturer not found.");
            return;
        }

        analyzeLikertResponses(lecturer, courseCode);
        analyzeOpenEndedResponses(lecturer, courseCode);
    }

    private void analyzeLikertResponses(Lecturer lecturer, String courseCode) {
        likertBarChart.getData().clear();
        Map<Questions, List<ResponseLikert>> responsesGrouped = new HashMap<>();

        // Get course evaluations for the selected lecturer and course
        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        for (CourseEval eval : allEvals) {
            if (eval.getLecturer().getFName().equals(lecturer.getFName()) && 
                eval.getLecturer().getLName().equals(lecturer.getLName()) &&
                eval.getCourse().getClassCode().getcourseCode().equals(courseCode)) {
                
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

    private void analyzeOpenEndedResponses(Lecturer lecturer, String courseCode) {
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
                eval.getCourse().getClassCode().getcourseCode().equals(courseCode)) {
                
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

    private Lecturer findLecturerByName(String firstName, String lastName) {
        if (firstName == null || lastName == null || 
            firstName.trim().isEmpty() || lastName.trim().isEmpty()) {
            return null;
        }
        
        firstName = firstName.trim();
        lastName = lastName.trim();
        
        Iterable<Lecturer> lecturers = lecturerRepository.findAll();
        if (lecturers == null) {
            return null;
        }
        
        for (Lecturer lecturer : lecturers) {
            if (lecturer != null && 
                lecturer.getFName() != null && 
                lecturer.getLName() != null &&
                lecturer.getFName().equalsIgnoreCase(firstName) && 
                lecturer.getLName().equalsIgnoreCase(lastName)) {
                return lecturer;
            }
        }
        return null;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Analysis");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation methods
    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "CourseSurvey.fxml", springContext);
    }

    @FXML
    public void handleBackMain(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "main-view.fxml", springContext);
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
} 
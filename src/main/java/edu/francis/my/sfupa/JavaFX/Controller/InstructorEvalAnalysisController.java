package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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
    public void initialize() {
        if (semesterCmb != null) {
            semesterCmb.setItems(FXCollections.observableArrayList("Spring", "Summer", "Fall", "Winter"));
        }
        if (courseCmb != null) {
            courseCmb.setItems(FXCollections.observableArrayList(
                    "Health 101", "Health Science 213", "Heart Studies 340", "CS101"
            ));
        }

           /*
        if (courseCombo != null) {
            Iterable<Course> courses = courseRepository.findAll();
            List<String> courseNames = new ArrayList<>();

            for (Course course : courses) {
                courseNames.add(course.getName());
            }

            courseCombo.setItems(FXCollections.observableArrayList(courseNames));
        }
        */


        if (yearCmb != null) {
            // Fetch school years from the repository
            List<SchoolYear> schoolYears = (List<SchoolYear>) schoolYearRepository.findAll();

            // Convert school year names (like "2023-2024") into a list of strings
            List<String> schoolYearNames = schoolYears.stream()
                    .map(SchoolYear::getName)  // Assuming you have getName() method for the "name" field
                    .collect(Collectors.toList());

            // Set the items for yearCombo
            yearCmb.setItems(FXCollections.observableArrayList(schoolYearNames));
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

        for (ResponseLikert response : responseLikertRepository.findAll()) {
            responsesGrouped
                    .computeIfAbsent(response.getQuestion(), k -> new ArrayList<>())
                    .add(response);
        }

        for (Map.Entry<Questions, List<ResponseLikert>> entry : responsesGrouped.entrySet()) {
            double avgResponse = 0.0;
            List<ResponseLikert> responses = entry.getValue();
            if (!responses.isEmpty()) {
                double sum = 0;
                for (ResponseLikert response : responses) {
                    sum += mapLikertToNumeric(response.getResponse());
                }
                avgResponse = sum / responses.size();
            }

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(entry.getKey().getText());
            series.getData().add(new XYChart.Data<>("Average", avgResponse));

            likertBarChart.getData().add(series);
        }
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

    private void analyzeOpenEndedResponses() {
        List<ResponseOpen> openResponses = (List<ResponseOpen>) responseOpenRepository.findAll();
        if (!openResponses.isEmpty()) {
            String aggregatedResponses = openResponses.stream()
                    .map(ResponseOpen::getResponse)
                    .collect(Collectors.joining("\n\n"));
            openEndedAnalysisArea.setText(performBasicTextAnalysis(aggregatedResponses));
        } else {
            openEndedAnalysisArea.setText("No open-ended responses found.");
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
}

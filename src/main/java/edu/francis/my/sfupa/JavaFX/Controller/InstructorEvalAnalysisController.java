package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static edu.francis.my.sfupa.HelloFXApplication.springContext;
import com.opencsv.CSVWriter;
import javafx.stage.FileChooser;

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
    @FXML private TableView<ResponseData> rawResponsesTable;
    @FXML private TableColumn<ResponseData, String> questionColumn;
    @FXML private TableColumn<ResponseData, String> responseColumn;
    @FXML private TextArea thematicAnalysisArea;

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

        // Update raw data table
        updateRawResponsesTable(relevantResponses);
        
        // Perform thematic analysis
        performThematicAnalysis(relevantResponses);
    }

    private void updateRawResponsesTable(List<ResponseOpen> responses) {
        ObservableList<ResponseData> tableData = FXCollections.observableArrayList();
        
        for (ResponseOpen response : responses) {
            tableData.add(new ResponseData(
                response.getQuestion().getText(),
                response.getResponse()
            ));
        }
        
        questionColumn.setCellValueFactory(new PropertyValueFactory<>("question"));
        responseColumn.setCellValueFactory(new PropertyValueFactory<>("response"));
        rawResponsesTable.setItems(tableData);
    }

    private void performThematicAnalysis(List<ResponseOpen> responses) {
        if (responses.isEmpty()) {
            thematicAnalysisArea.setText("No open-ended responses found for the selected criteria.");
            return;
        }

        StringBuilder analysis = new StringBuilder();
        analysis.append("Thematic Analysis Summary\n");
        analysis.append("========================\n\n");

        // Group responses by question
        Map<Questions, List<String>> responsesByQuestion = responses.stream()
            .collect(Collectors.groupingBy(
                ResponseOpen::getQuestion,
                Collectors.mapping(ResponseOpen::getResponse, Collectors.toList())
            ));

        // Analyze each question's responses
        for (Map.Entry<Questions, List<String>> entry : responsesByQuestion.entrySet()) {
            String questionText = entry.getKey().getText();
            List<String> questionResponses = entry.getValue();

            analysis.append("Question: ").append(questionText).append("\n");
            analysis.append("----------------------------------------\n");

            // Extract key themes
            List<String> themes = extractThemes(questionResponses);
            analysis.append("Key Themes:\n");
            for (String theme : themes) {
                analysis.append("- ").append(theme).append("\n");
            }
            analysis.append("\n");

            // Calculate sentiment
            String sentiment = calculateSentiment(questionResponses);
            analysis.append("Overall Sentiment: ").append(sentiment).append("\n\n");
        }

        thematicAnalysisArea.setText(analysis.toString());
    }

    private List<String> extractThemes(List<String> responses) {
        // Common words to ignore
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "the", "and", "a", "an", "in", "on", "at", "to", "for", "of", "with", "by",
            "this", "that", "these", "those", "is", "are", "was", "were", "be", "been", "being",
            "have", "has", "had", "do", "does", "did", "will", "would", "should", "could",
            "can", "may", "might", "must", "shall", "should", "ought", "need", "dare"
        ));

        // Extract words and count frequencies
        Map<String, Integer> wordFrequencies = new HashMap<>();
        for (String response : responses) {
            String[] words = response.toLowerCase().split("\\s+");
            for (String word : words) {
                // Remove punctuation and check if it's a meaningful word
                word = word.replaceAll("[^a-zA-Z]", "");
                if (word.length() > 3 && !stopWords.contains(word)) {
                    wordFrequencies.merge(word, 1, Integer::sum);
                }
            }
        }

        // Get top themes
        return wordFrequencies.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private String calculateSentiment(List<String> responses) {
        // Simple sentiment analysis based on positive/negative words
        Set<String> positiveWords = new HashSet<>(Arrays.asList(
            "good", "great", "excellent", "amazing", "wonderful", "helpful", "clear",
            "understand", "learn", "enjoy", "love", "like", "appreciate", "thank"
        ));
        
        Set<String> negativeWords = new HashSet<>(Arrays.asList(
            "bad", "poor", "terrible", "confusing", "difficult", "hard", "problem",
            "issue", "don't", "doesn't", "didn't", "wasn't", "weren't", "can't"
        ));

        int positiveCount = 0;
        int negativeCount = 0;

        for (String response : responses) {
            String[] words = response.toLowerCase().split("\\s+");
            for (String word : words) {
                word = word.replaceAll("[^a-zA-Z]", "");
                if (positiveWords.contains(word)) {
                    positiveCount++;
                } else if (negativeWords.contains(word)) {
                    negativeCount++;
                }
            }
        }

        if (positiveCount > negativeCount * 2) {
            return "Very Positive";
        } else if (positiveCount > negativeCount) {
            return "Positive";
        } else if (negativeCount > positiveCount * 2) {
            return "Very Negative";
        } else if (negativeCount > positiveCount) {
            return "Negative";
        } else {
            return "Neutral";
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

    @FXML
    private void handleExportCSV() {
        if (courseCmb.getValue() == null || yearCmb.getValue() == null || semesterCmb.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select Course, Year, and Semester before exporting");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Evaluation Data");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("instructor_evaluation_data.csv");

        File file = fileChooser.showSaveDialog(rawResponsesTable.getScene().getWindow());
        if (file != null) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                // Write header
                writer.writeNext(new String[]{"Question", "Response", "Response Type"});

                // Write Likert responses
                for (ResponseLikert response : responseLikertRepository.findAll()) {
                    if (response.getCourseEval().getCourse().getClassCode().getcourseCode().equals(courseCmb.getValue())) {
                        writer.writeNext(new String[]{
                            response.getQuestion().getText(),
                            response.getResponse(),
                            "Likert"
                        });
                    }
                }

                // Write open-ended responses
                for (ResponseOpen response : responseOpenRepository.findAll()) {
                    if (response.getCourseEval().getCourse().getClassCode().getcourseCode().equals(courseCmb.getValue())) {
                        writer.writeNext(new String[]{
                            response.getQuestion().getText(),
                            response.getResponse(),
                            "Open-ended"
                        });
                    }
                }

                showAlert("Data exported successfully to " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Failed to export data: " + e.getMessage());
            }
        }
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
    
    // Helper class for table data
    public static class ResponseData {
        private final String question;
        private final String response;

        public ResponseData(String question, String response) {
            this.question = question;
            this.response = response;
        }

        public String getQuestion() {
            return question;
        }

        public String getResponse() {
            return response;
        }
    }
}

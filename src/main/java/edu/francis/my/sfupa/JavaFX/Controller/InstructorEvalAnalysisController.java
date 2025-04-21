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
import java.util.stream.StreamSupport;

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
    @Autowired private ClassesRepository classesRepository;

    @FXML
    public void initialize() {
        setupCourseComboBox();
        setupChartStyle();

        // Add listener for course combo box updates
        courseCmb.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateYearOptions(newVal);
            }
        });

        // Add listener for year combo box updates
        yearCmb.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && courseCmb.getValue() != null) {
                updateSemesterOptions(courseCmb.getValue(), newVal);
            }
        });
    }

    private void setupCourseComboBox() {
        Set<String> uniqueCourses = new HashSet<>();
        for (CourseEval eval : courseEvalRepository.findAll()) {
            if (eval.getEvalType() == CourseEval.EvalType.INSTRUCTOR && 
                eval.getCourse() != null && 
                eval.getCourse().getClassCode() != null) {
                uniqueCourses.add(eval.getCourse().getClassCode().getcourseCode());
            }
        }
        List<String> sortedCourses = new ArrayList<>(uniqueCourses);
        Collections.sort(sortedCourses);
        courseCmb.setItems(FXCollections.observableArrayList(sortedCourses));
    }

    private void updateYearOptions(String selectedCourse) {
        // Get all evaluations for this course
        List<CourseEval> courseEvals = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
                .filter(eval -> eval.getEvalType() == CourseEval.EvalType.INSTRUCTOR) // Only get instructor evaluations
                .filter(eval -> eval.getCourse() != null &&
                        eval.getCourse().getClassCode() != null &&
                        eval.getCourse().getClassCode().getcourseCode().equals(selectedCourse))
                .collect(Collectors.toList());

        // Get years when this course was evaluated
        List<String> years = courseEvals.stream()
                .map(eval -> eval.getCourse().getSchoolYear().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        yearCmb.setItems(FXCollections.observableArrayList(years));
        yearCmb.getSelectionModel().clearSelection();
        semesterCmb.getSelectionModel().clearSelection();
        semesterCmb.setItems(FXCollections.observableArrayList()); // Clear semester options until year is selected
    }

    private void updateSemesterOptions(String selectedCourse, String selectedYear) {
        // Get all evaluations for this course and year
        List<CourseEval> filteredEvals = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
                .filter(eval -> eval.getEvalType() == CourseEval.EvalType.INSTRUCTOR) // Only get instructor evaluations
                .filter(eval -> eval.getCourse() != null &&
                        eval.getCourse().getClassCode() != null &&
                        eval.getCourse().getClassCode().getcourseCode().equals(selectedCourse) &&
                        eval.getCourse().getSchoolYear().getName().equals(selectedYear))
                .collect(Collectors.toList());

        // Get semesters when this course was evaluated in the selected year
        List<String> semesters = filteredEvals.stream()
                .map(eval -> eval.getCourse().getSemester().getName().name())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        semesterCmb.setItems(FXCollections.observableArrayList(semesters));
        semesterCmb.getSelectionModel().clearSelection();
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
            
            StringBuilder missingFields = new StringBuilder("Please select the following fields:\n");
            if (courseCmb.getValue() == null) missingFields.append("- Course\n");
            if (yearCmb.getValue() == null) missingFields.append("- Year\n");
            if (semesterCmb.getValue() == null) missingFields.append("- Semester\n");
            
            showAlert(missingFields.toString());
            return;
        }

        analyzeLikertResponses();
        analyzeOpenEndedResponses();
    }

    private void analyzeLikertResponses() {
        likertBarChart.getData().clear();
        Map<Questions, List<ResponseLikert>> responsesGrouped = new HashMap<>();

        // Get the selected course, year, and semester
        String selectedCourseCode = courseCmb.getValue();
        String selectedYearName = yearCmb.getValue();
        String selectedSemesterName = semesterCmb.getValue();

        // Find the corresponding entities
        Course selectedCourse = courseRepository.findByCourseCode(selectedCourseCode);
        SchoolYear selectedYear = schoolYearRepository.findByName(selectedYearName);
        SemesterName semesterEnum = SemesterName.fromString(selectedSemesterName);
        Semester selectedSemester = semesterRepository.findById(semesterEnum.getId()).orElse(null);

        if (selectedCourse == null || selectedYear == null || selectedSemester == null) {
            showAlert("Could not find matching course, year, or semester");
            return;
        }

        // Find the specific class
        Classes classEntity = classesRepository.findByClassCode_CourseCodeAndSemester_IdAndSchoolYear_IdSchoolYear(
                selectedCourseCode, semesterEnum.getId(), selectedYear.getIdSchoolYear().intValue())
                .orElse(null);

        if (classEntity == null) {
            showAlert("No class found for the selected criteria");
            return;
        }

        // Get all evaluations for this specific class
        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        for (CourseEval eval : allEvals) {
            // Check if it's an instructor evaluation and matches the criteria
            if (eval.getEvalType() == CourseEval.EvalType.INSTRUCTOR &&
                eval.getCourse() != null && 
                eval.getCourse().getIdClass().equals(classEntity.getIdClass())) {
                
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
        String selectedCourseCode = courseCmb.getValue();
        String selectedYearName = yearCmb.getValue();
        String selectedSemesterName = semesterCmb.getValue();

        if (selectedCourseCode == null || selectedYearName == null || selectedSemesterName == null) {
            showAlert("Please select Course, Year, and Semester");
            return;
        }

        // Find the corresponding entities
        Course selectedCourse = courseRepository.findByCourseCode(selectedCourseCode);
        SchoolYear selectedYear = schoolYearRepository.findByName(selectedYearName);
        SemesterName semesterEnum = SemesterName.fromString(selectedSemesterName);
        Semester selectedSemester = semesterRepository.findById(semesterEnum.getId()).orElse(null);

        if (selectedCourse == null || selectedYear == null || selectedSemester == null) {
            showAlert("Could not find matching course, year, or semester");
            return;
        }

        // Find the specific class
        Classes classEntity = classesRepository.findByClassCode_CourseCodeAndSemester_IdAndSchoolYear_IdSchoolYear(
                selectedCourseCode, semesterEnum.getId(), selectedYear.getIdSchoolYear().intValue())
                .orElse(null);

        if (classEntity == null) {
            showAlert("No class found for the selected criteria");
            return;
        }

        // Get all evaluations for this specific class
        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        List<ResponseOpen> relevantResponses = new ArrayList<>();

        for (CourseEval eval : allEvals) {
            // Check if it's an instructor evaluation and matches the criteria
            if (eval.getEvalType() == CourseEval.EvalType.INSTRUCTOR &&
                eval.getCourse() != null && 
                eval.getCourse().getClassCode() != null &&
                eval.getCourse().getClassCode().getcourseCode().equals(selectedCourseCode) &&
                eval.getCourse().getIdClass().equals(classEntity.getIdClass())) {
                
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

        // Get the selected course, year, and semester
        String selectedCourseCode = courseCmb.getValue();
        String selectedYearName = yearCmb.getValue();
        String selectedSemesterName = semesterCmb.getValue();

        // Find the corresponding entities
        Course selectedCourse = courseRepository.findByCourseCode(selectedCourseCode);
        SchoolYear selectedYear = schoolYearRepository.findByName(selectedYearName);
        SemesterName semesterEnum = SemesterName.fromString(selectedSemesterName);
        Semester selectedSemester = semesterRepository.findById(semesterEnum.getId()).orElse(null);

        if (selectedCourse == null || selectedYear == null || selectedSemester == null) {
            showAlert("Could not find matching course, year, or semester");
            return;
        }

        // Find the specific class
        Classes classEntity = classesRepository.findByClassCode_CourseCodeAndSemester_IdAndSchoolYear_IdSchoolYear(
                selectedCourseCode, semesterEnum.getId(), selectedYear.getIdSchoolYear().intValue())
                .orElse(null);

        if (classEntity == null) {
            showAlert("No class found for the selected criteria");
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

                // Get all evaluations for this specific class
                List<CourseEval> relevantEvals = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
                    .filter(eval -> eval.getEvalType() == CourseEval.EvalType.INSTRUCTOR) // Only get instructor evaluations
                    .filter(eval -> eval.getCourse() != null && 
                            eval.getCourse().getIdClass().equals(classEntity.getIdClass()))
                    .collect(Collectors.toList());

                // Write Likert responses
                for (CourseEval eval : relevantEvals) {
                    for (ResponseLikert response : responseLikertRepository.findAll()) {
                        if (response.getCourseEval().getId().equals(eval.getId())) {
                            writer.writeNext(new String[]{
                                response.getQuestion().getText(),
                                response.getResponse(),
                                "Likert"
                            });
                        }
                    }
                }

                // Write open-ended responses
                for (CourseEval eval : relevantEvals) {
                    for (ResponseOpen response : responseOpenRepository.findAll()) {
                        if (response.getCourseEval().getId().equals(eval.getId())) {
                            writer.writeNext(new String[]{
                                response.getQuestion().getText(),
                                response.getResponse(),
                                "Open-ended"
                            });
                        }
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

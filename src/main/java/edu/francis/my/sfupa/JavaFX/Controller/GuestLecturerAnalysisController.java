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
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.opencsv.CSVWriter;
import javafx.stage.FileChooser;

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
    private TableView<ResponseData> rawResponsesTable;
    @FXML
    private TableColumn<ResponseData, String> questionColumn;
    @FXML
    private TableColumn<ResponseData, String> responseColumn;
    @FXML
    private TextArea thematicAnalysisArea;

    @FXML
    public void initialize() {
        setupLecturerComboBox();
        setupYearComboBox();
        setupSemesterComboBox();
        setupChartStyle();

        // Add listener for lecturer combo box updates
        lecturerCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateCourseComboBox(newVal);
            }
        });

        // Add listener for course combo box updates
        courseCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && lecturerCombo.getValue() != null) {
                updateYearAndSemesterOptions(lecturerCombo.getValue(), newVal);
            }
        });

        // Add listener for year combo box updates
        yearCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && courseCombo.getValue() != null && lecturerCombo.getValue() != null) {
                updateSemesterOptions(lecturerCombo.getValue(), courseCombo.getValue(), newVal);
            }
        });
    }

    private void setupLecturerComboBox() {
        // Get all lecturers
        List<Lecturer> lecturers = StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        // Create list of lecturer names
        List<String> lecturerNames = lecturers.stream()
                .map(l -> l.getFName() + " " + l.getLName())
                .collect(Collectors.toList());

        lecturerCombo.setItems(FXCollections.observableArrayList(lecturerNames));
    }

    private void updateCourseComboBox(String lecturerName) {
        if (lecturerName == null || lecturerName.trim().isEmpty()) {
            return;
        }

        String[] nameParts = lecturerName.split(" ", 2);
        if (nameParts.length < 2) {
            return;
        }

        String firstName = nameParts[0].trim();
        String lastName = nameParts[1].trim();

        // Find the lecturer
        Lecturer lecturer = findLecturerByName(firstName, lastName);
        if (lecturer == null) {
            return;
        }

        // Get all course evaluations for this lecturer
        List<CourseEval> lecturerEvals = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
                .filter(eval -> eval.getEvalType() == CourseEval.EvalType.GUEST_LECTURER) // Only get guest lecturer evaluations
                .filter(eval -> eval.getLecturer() != null &&
                        eval.getLecturer().getId().equals(lecturer.getId()) &&
                        eval.getCourse() != null &&
                        eval.getCourse().getClassCode() != null)
                .collect(Collectors.toList());

        // Update courses dropdown
        List<String> courses = lecturerEvals.stream()
                .map(eval -> {
                    Course course = eval.getCourse().getClassCode();
                    return course.getcourseCode() + " - " + course.getName();
                })
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        courseCombo.setItems(FXCollections.observableArrayList(courses));
        
        // Update years dropdown based on lecturer's teaching history
        List<String> years = lecturerEvals.stream()
                .map(eval -> eval.getCourse().getSchoolYear().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        yearCombo.setItems(FXCollections.observableArrayList(years));

        // Update semesters dropdown based on lecturer's teaching history
        List<String> semesters = lecturerEvals.stream()
                .map(eval -> eval.getCourse().getSemester().getName().name())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        semesterCombo.setItems(FXCollections.observableArrayList(semesters));
        
        // Clear previous selections
        courseCombo.getSelectionModel().clearSelection();
        yearCombo.getSelectionModel().clearSelection();
        semesterCombo.getSelectionModel().clearSelection();

        if (!courses.isEmpty()) {
            courseCombo.getSelectionModel().selectFirst();
        }
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
        // Clear previous data
        likertBarChart.getData().clear();
        thematicAnalysisArea.clear();
        if (rawResponsesTable != null) {
            rawResponsesTable.getItems().clear();
        }

        // Validate all required fields
        if (lecturerCombo.getValue() == null || courseCombo.getValue() == null ||
                yearCombo.getValue() == null || semesterCombo.getValue() == null) {
            
            StringBuilder missingFields = new StringBuilder("Please select the following fields:\n");
            if (lecturerCombo.getValue() == null) missingFields.append("- Guest Lecturer\n");
            if (courseCombo.getValue() == null) missingFields.append("- Course\n");
            if (yearCombo.getValue() == null) missingFields.append("- Year\n");
            if (semesterCombo.getValue() == null) missingFields.append("- Semester\n");
            
            showAlert(missingFields.toString());
            return;
        }

        String[] lecturerName = lecturerCombo.getValue().split(" ");
        String courseCode = courseCombo.getValue().split(" - ")[0];
        String year = yearCombo.getValue();
        String semester = semesterCombo.getValue();

        // Find the lecturer
        Lecturer lecturer = null;
        if (lecturerName.length >= 2) {
            lecturer = findLecturerByName(lecturerName[0], lecturerName[1]);
            if (lecturer == null) {
                showAlert("Lecturer not found in the database.");
                return;
            }
        }

        // Analyze the data
        analyzeLikertResponses(lecturer, courseCode);
        analyzeOpenEndedResponses(lecturer, courseCode);
    }

    private void analyzeLikertResponses(Lecturer lecturer, String courseCode) {
        likertBarChart.getData().clear();
        Map<Questions, List<ResponseLikert>> responsesGrouped = new HashMap<>();

        String selectedYear = yearCombo.getValue();
        String selectedSemester = semesterCombo.getValue();

        // Get course evaluations for the selected lecturer and course
        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        for (CourseEval eval : allEvals) {
            // Check if it's a guest lecturer evaluation and matches the criteria
            if (eval.getEvalType() == CourseEval.EvalType.GUEST_LECTURER &&
                eval.getCourse() != null && 
                eval.getCourse().getClassCode() != null &&
                eval.getCourse().getClassCode().getcourseCode().equals(courseCode) &&
                eval.getCourse().getSchoolYear().getName().equals(selectedYear) &&
                eval.getCourse().getSemester().getName().name().equals(selectedSemester)) {
                
                // If lecturer is specified, check if it matches
                if (lecturer != null) {
                    if (eval.getLecturer() == null || 
                        !eval.getLecturer().getFName().equals(lecturer.getFName()) || 
                        !eval.getLecturer().getLName().equals(lecturer.getLName())) {
                        continue;
                    }
                }
                
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
        // Get all relevant responses
        List<ResponseOpen> relevantResponses = getRelevantResponses(lecturer, courseCode);
        
        // Update raw data table
        updateRawResponsesTable(relevantResponses);
        
        // Perform thematic analysis
        performThematicAnalysis(relevantResponses);
    }

    private List<ResponseOpen> getRelevantResponses(Lecturer lecturer, String courseCode) {
        List<ResponseOpen> relevantResponses = new ArrayList<>();
        String selectedYear = yearCombo.getValue();
        String selectedSemester = semesterCombo.getValue();
        
        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        
        for (CourseEval eval : allEvals) {
            // Check if it's a guest lecturer evaluation and matches the criteria
            if (eval.getEvalType() == CourseEval.EvalType.GUEST_LECTURER &&
                eval.getCourse() != null && 
                eval.getCourse().getClassCode() != null &&
                eval.getCourse().getClassCode().getcourseCode().equals(courseCode) &&
                eval.getCourse().getSchoolYear().getName().equals(selectedYear) &&
                eval.getCourse().getSemester().getName().name().equals(selectedSemester)) {
                
                if (lecturer != null) {
                    if (eval.getLecturer() == null || 
                        !eval.getLecturer().getFName().equals(lecturer.getFName()) || 
                        !eval.getLecturer().getLName().equals(lecturer.getLName())) {
                        continue;
                    }
                }
                
                for (ResponseOpen response : responseOpenRepository.findAll()) {
                    if (response.getCourseEval().getId().equals(eval.getId())) {
                        relevantResponses.add(response);
                    }
                }
            }
        }
        return relevantResponses;
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
        try {
            return StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
                    .filter(l -> l.getFName().equals(firstName) && l.getLName().equals(lastName))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    @FXML
    private void handleExportCSV() {
        if (lecturerCombo.getValue() == null || courseCombo.getValue() == null ||
            yearCombo.getValue() == null || semesterCombo.getValue() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select Lecturer, Course, Year, and Semester before exporting");
            alert.showAndWait();
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Guest Lecturer Evaluation Data");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("guest_lecturer_evaluation_data.csv");

        File file = fileChooser.showSaveDialog(rawResponsesTable.getScene().getWindow());
        if (file != null) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                // Write header
                writer.writeNext(new String[]{"Question", "Response", "Response Type"});

                String[] lecturerName = lecturerCombo.getValue().split(" ");
                String firstName = lecturerName[0];
                String lastName = lecturerName[1];
                Lecturer lecturer = findLecturerByName(firstName, lastName);

                if (lecturer != null) {
                    // Write Likert responses
                    for (ResponseLikert response : responseLikertRepository.findAll()) {
                        CourseEval eval = response.getCourseEval();
                        if (eval.getLecturer() != null && 
                            eval.getLecturer().getId().equals(lecturer.getId()) &&
                            eval.getCourse() != null &&
                            eval.getCourse().getClassCode() != null &&
                            eval.getCourse().getClassCode().getcourseCode().equals(courseCombo.getValue().split(" - ")[0])) {
                            writer.writeNext(new String[]{
                                response.getQuestion().getText(),
                                response.getResponse(),
                                "Likert"
                            });
                        }
                    }

                    // Write open-ended responses
                    for (ResponseOpen response : responseOpenRepository.findAll()) {
                        CourseEval eval = response.getCourseEval();
                        if (eval.getLecturer() != null && 
                            eval.getLecturer().getId().equals(lecturer.getId()) &&
                            eval.getCourse() != null &&
                            eval.getCourse().getClassCode() != null &&
                            eval.getCourse().getClassCode().getcourseCode().equals(courseCombo.getValue().split(" - ")[0])) {
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

    private void updateSemesterOptions(String lecturerName, String courseWithName, String selectedYear) {
        String[] nameParts = lecturerName.split(" ", 2);
        if (nameParts.length < 2) return;

        String firstName = nameParts[0].trim();
        String lastName = nameParts[1].trim();
        String courseCode = courseWithName.split(" - ")[0];

        // Find the lecturer
        Lecturer lecturer = findLecturerByName(firstName, lastName);
        if (lecturer == null) return;

        // Get all course evaluations for this lecturer, course, and year
        List<CourseEval> filteredEvals = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
                .filter(eval -> eval.getLecturer() != null &&
                        eval.getLecturer().getId().equals(lecturer.getId()) &&
                        eval.getCourse() != null &&
                        eval.getCourse().getClassCode() != null &&
                        eval.getCourse().getClassCode().getcourseCode().equals(courseCode) &&
                        eval.getCourse().getSchoolYear().getName().equals(selectedYear))
                .collect(Collectors.toList());

        // Update semesters dropdown for this specific course and year
        List<String> semesters = filteredEvals.stream()
                .map(eval -> eval.getCourse().getSemester().getName().name())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        semesterCombo.setItems(FXCollections.observableArrayList(semesters));
        semesterCombo.getSelectionModel().clearSelection();
    }

    private void updateYearAndSemesterOptions(String lecturerName, String courseWithName) {
        String[] nameParts = lecturerName.split(" ", 2);
        if (nameParts.length < 2) return;

        String firstName = nameParts[0].trim();
        String lastName = nameParts[1].trim();
        String courseCode = courseWithName.split(" - ")[0];

        // Find the lecturer
        Lecturer lecturer = findLecturerByName(firstName, lastName);
        if (lecturer == null) return;

        // Get all course evaluations for this lecturer and course
        List<CourseEval> lecturerCourseEvals = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
                .filter(eval -> eval.getLecturer() != null &&
                        eval.getLecturer().getId().equals(lecturer.getId()) &&
                        eval.getCourse() != null &&
                        eval.getCourse().getClassCode() != null &&
                        eval.getCourse().getClassCode().getcourseCode().equals(courseCode))
                .collect(Collectors.toList());

        // Update years dropdown for this specific course
        List<String> years = lecturerCourseEvals.stream()
                .map(eval -> eval.getCourse().getSchoolYear().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        yearCombo.setItems(FXCollections.observableArrayList(years));

        // Clear previous selections
        yearCombo.getSelectionModel().clearSelection();
        semesterCombo.getSelectionModel().clearSelection();
        semesterCombo.setItems(FXCollections.observableArrayList()); // Clear semester options until year is selected
    }
} 
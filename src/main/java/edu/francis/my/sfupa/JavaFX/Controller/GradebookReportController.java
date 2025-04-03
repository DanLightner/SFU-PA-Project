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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class GradebookReportController {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @FXML private ComboBox<String> courseCmb;
    @FXML private ComboBox<String> yearCmb;
    @FXML private ComboBox<String> semesterCmb;
    @FXML private BarChart<String, Number> gradeBarChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;
    @FXML private TableView<RetakeStudent> retakeTable;
    @FXML private TableColumn<RetakeStudent, String> studentIdColumn;
    @FXML private TableColumn<RetakeStudent, String> gradeColumn;

    @FXML
    public void initialize() {
        setupCourseComboBox();
        setupYearComboBox();
        setupSemesterComboBox();
        setupTableView();
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

    private void setupYearComboBox() {
        if (yearCmb != null) {
            List<SchoolYear> schoolYears = new ArrayList<>();
            schoolYearRepository.findAll().forEach(schoolYears::add);
            List<String> yearNames = schoolYears.stream()
                    .map(SchoolYear::getName)
                    .collect(Collectors.toList());
            yearCmb.setItems(FXCollections.observableArrayList(yearNames));
        }
    }

    private void setupSemesterComboBox() {
        if (semesterCmb != null) {
            semesterCmb.setItems(FXCollections.observableArrayList("Spring", "Summer", "Fall", "Winter"));
        }
    }

    private void setupTableView() {
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
    }

    private void setupChartStyle() {
        if (gradeBarChart != null) {
            gradeBarChart.setStyle("-fx-background-color: white;");
            gradeBarChart.setTitle("Grade Distribution");
            
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
    public void generateReport() {
        if (courseCmb.getValue() == null || yearCmb.getValue() == null || semesterCmb.getValue() == null) {
            showAlert("Please select Course, Year, and Semester");
            return;
        }

        try {
            // Get selected values
            String courseCode = courseCmb.getValue();
            String yearName = yearCmb.getValue();
            String semesterName = semesterCmb.getValue();

            // Find the class
            Course course = courseRepository.findByCourseCode(courseCode);
            SchoolYear year = schoolYearRepository.findByName(yearName);
            SemesterName semesterEnum = SemesterName.fromString(semesterName);
            Semester semester = semesterRepository.findById(semesterEnum.getId()).orElse(null);

            if (course == null || year == null || semester == null) {
                showAlert("Could not find matching course, year, or semester");
                return;
            }

            Classes classEntity = classesRepository.findByClassCode_CourseCodeAndSemester_IdAndSchoolYear_IdSchoolYear(
                    courseCode, semesterEnum.getId(), year.getIdSchoolYear().intValue())
                    .orElse(null);

            if (classEntity == null) {
                showAlert("No grades found for the selected criteria");
                return;
            }

            // Get all grades for this class
            List<Grade> grades = gradeRepository.findByStudentClass(classEntity);

            // Update grade distribution chart
            updateGradeDistributionChart(grades);

            // Update retake table
            updateRetakeTable(grades);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error generating report: " + e.getMessage());
        }
    }

    private void updateGradeDistributionChart(List<Grade> grades) {
        gradeBarChart.getData().clear();

        // Define the standard grade order
        List<String> standardGradeOrder = List.of(
            "A+", "A", "A-",
            "B+", "B", "B-",
            "C+", "C", "C-",
            "D+", "D", "D-",
            "F"
        );

        // Initialize counts for all possible grades
        Map<String, Long> gradeCounts = standardGradeOrder.stream()
            .collect(Collectors.toMap(
                grade -> grade,
                grade -> 0L,
                (v1, v2) -> v1,
                LinkedHashMap::new
            ));

        // Count the actual grades
        grades.stream()
            .map(grade -> grade.getGrade().trim().toUpperCase())
            .forEach(grade -> {
                if (gradeCounts.containsKey(grade)) {
                    gradeCounts.put(grade, gradeCounts.get(grade) + 1);
                }
            });

        // Create chart data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Number of Students");

        // Add data points in the standard order
        standardGradeOrder.forEach(grade -> 
            series.getData().add(new XYChart.Data<>(grade, gradeCounts.get(grade)))
        );

        gradeBarChart.getData().add(series);

        // Style the chart
        gradeBarChart.setCategoryGap(0);
        gradeBarChart.setBarGap(2);

        // Add CSS style for different grade groups
        series.getData().forEach(data -> {
            String grade = data.getXValue();
            String color;
            if (grade.startsWith("A")) {
                color = "#2ecc71"; // Green for A's
            } else if (grade.startsWith("B")) {
                color = "#3498db"; // Blue for B's
            } else if (grade.startsWith("C")) {
                color = "#f1c40f"; // Yellow for C's
            } else if (grade.startsWith("D")) {
                color = "#e67e22"; // Orange for D's
            } else {
                color = "#e74c3c"; // Red for F
            }
            data.getNode().setStyle("-fx-bar-fill: " + color + ";");
        });
    }

    private void updateRetakeTable(List<Grade> grades) {
        ObservableList<RetakeStudent> retakeStudents = FXCollections.observableArrayList();

        // Filter grades that require retake (C or below)
        grades.stream()
                .filter(grade -> isGradeCOrBelow(grade.getGrade()))
                .forEach(grade -> 
                    retakeStudents.add(new RetakeStudent(
                        grade.getStudent().getId_student().toString(),
                        grade.getGrade()
                    ))
                );

        retakeTable.setItems(retakeStudents);
    }

    private boolean isGradeCOrBelow(String grade) {
        grade = grade.trim().toUpperCase();
        return grade.startsWith("C") || grade.equals("D+") || grade.equals("D") || 
               grade.equals("D-") || grade.equals("F");
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gradebook Report");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation methods
    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "Gradebook.fxml", springContext);
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

// Data class for retake table
class RetakeStudent {
    private String studentId;
    private String grade;

    public RetakeStudent(String studentId, String grade) {
        this.studentId = studentId;
        this.grade = grade;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
} 
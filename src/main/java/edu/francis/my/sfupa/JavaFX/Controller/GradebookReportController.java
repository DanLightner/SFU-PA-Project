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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import com.opencsv.CSVWriter;
import javafx.stage.FileChooser;
import javafx.beans.property.SimpleStringProperty;
import java.util.stream.StreamSupport;

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
        setupTableView();
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
        if (courseCmb != null) {
            List<Course> courses = new ArrayList<>();
            courseRepository.findAll().forEach(courses::add);
            
            List<String> courseOptions = courses.stream()
                    .map(course -> course.getcourseCode() + " - " + course.getName())
                    .collect(Collectors.toList());

            // Sort by PA number
            Collections.sort(courseOptions, (a, b) -> {
                String numA = a.replaceAll("\\D+", "");
                String numB = b.replaceAll("\\D+", "");
                return Integer.compare(Integer.parseInt(numA), Integer.parseInt(numB));
            });

            courseCmb.setItems(FXCollections.observableArrayList(courseOptions));
        }
    }

    private void updateYearOptions(String selectedCourse) {
        // Extract course code from the selected item (format: "PA XXX - Course Name")
        String courseCode = selectedCourse.split(" - ")[0];
        
        // Get all classes for this course
        List<Classes> classes = StreamSupport.stream(classesRepository.findAll().spliterator(), false)
                .filter(cls -> cls.getClassCode() != null && 
                        cls.getClassCode().getcourseCode().equals(courseCode))
                .collect(Collectors.toList());

        // Get years when this course was offered
        List<String> years = classes.stream()
                .map(cls -> cls.getSchoolYear().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        yearCmb.setItems(FXCollections.observableArrayList(years));
        yearCmb.getSelectionModel().clearSelection();
        semesterCmb.getSelectionModel().clearSelection();
        semesterCmb.setItems(FXCollections.observableArrayList()); // Clear semester options until year is selected
    }

    private void updateSemesterOptions(String selectedCourse, String selectedYear) {
        // Extract course code from the selected item (format: "PA XXX - Course Name")
        String courseCode = selectedCourse.split(" - ")[0];
        
        // Get all classes for this course and year
        List<Classes> filteredClasses = StreamSupport.stream(classesRepository.findAll().spliterator(), false)
                .filter(cls -> cls.getClassCode() != null && 
                        cls.getClassCode().getcourseCode().equals(courseCode) &&
                        cls.getSchoolYear().getName().equals(selectedYear))
                .collect(Collectors.toList());

        // Get semesters when this course was offered in the selected year
        List<String> semesters = filteredClasses.stream()
                .map(cls -> cls.getSemester().getName().name())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        semesterCmb.setItems(FXCollections.observableArrayList(semesters));
        semesterCmb.getSelectionModel().clearSelection();
    }

    @FXML
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
            String courseCode = courseCmb.getValue().split(" - ")[0]; // Extract course code from the combined string
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
            "A", "A-",
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
        return grade.equals("C") || grade.equals("C-") || grade.equals("D+") || 
               grade.equals("D") || grade.equals("D-") || grade.equals("F");
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

    @FXML
    private void handleExportCSV() {
        if (courseCmb.getValue() == null || yearCmb.getValue() == null || semesterCmb.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please select Course, Year, and Semester before exporting");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Gradebook Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("gradebook_report.csv");

        File file = fileChooser.showSaveDialog(retakeTable.getScene().getWindow());
        if (file != null) {
            try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
                // Get selected values
                String courseCode = courseCmb.getValue().split(" - ")[0];
                String yearName = yearCmb.getValue();
                String semesterName = semesterCmb.getValue();

                // Find the class
                Course course = courseRepository.findByCourseCode(courseCode);
                SchoolYear year = schoolYearRepository.findByName(yearName);
                SemesterName semesterEnum = SemesterName.fromString(semesterName);
                Semester semester = semesterRepository.findById(semesterEnum.getId()).orElse(null);

                if (course == null || year == null || semester == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not find matching course, year, or semester");
                    return;
                }

                Classes classEntity = classesRepository.findByClassCode_CourseCodeAndSemester_IdAndSchoolYear_IdSchoolYear(
                    courseCode, semesterEnum.getId(), year.getIdSchoolYear().intValue())
                    .orElse(null);

                if (classEntity == null) {
                    showAlert(Alert.AlertType.ERROR, "Error", "No grades found for the selected criteria");
                    return;
                }

                // Get all grades for this class
                List<Grade> grades = gradeRepository.findByStudentClass(classEntity);

                // --- Write summary statistics ---
                writer.writeNext(new String[]{"SUMMARY STATISTICS"});
                writer.writeNext(new String[]{"Course", courseCmb.getValue(), "Year", yearName, "Semester", semesterName});
                writer.writeNext(new String[]{"Total Students", String.valueOf(grades.size())});

                // Grade distribution
                Map<String, Long> gradeCounts = new LinkedHashMap<>();
                List<String> standardGradeOrder = List.of(
                    "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F"
                );
                for (String g : standardGradeOrder) gradeCounts.put(g, 0L);
                for (Grade grade : grades) {
                    String g = grade.getGrade().trim().toUpperCase();
                    if (gradeCounts.containsKey(g)) {
                        gradeCounts.put(g, gradeCounts.get(g) + 1);
                    }
                }
                writer.writeNext(new String[]{"Grade Distribution:"});
                for (String g : standardGradeOrder) {
                    writer.writeNext(new String[]{g, String.valueOf(gradeCounts.get(g))});
                }

                // C or below count and percentage
                long cOrBelowCount = grades.stream().filter(g -> isGradeCOrBelow(g.getGrade())).count();
                double cOrBelowPercent = grades.size() > 0 ? (100.0 * cOrBelowCount / grades.size()) : 0.0;
                writer.writeNext(new String[]{"C or Below Count", String.valueOf(cOrBelowCount)});
                writer.writeNext(new String[]{"C or Below Percentage", String.format("%.2f%%", cOrBelowPercent)});

                // Average grade (if possible)
                double avgGrade = calculateAverageGrade(grades);
                if (avgGrade > 0) {
                    writer.writeNext(new String[]{"Average Grade (numeric)", String.format("%.2f", avgGrade)});
                }
                writer.writeNext(new String[]{""}); // Blank line

                // --- Write raw data ---
                writer.writeNext(new String[]{"Student ID", "Grade", "Retake Status"});
                for (Grade grade : grades) {
                    boolean needsRetake = isGradeCOrBelow(grade.getGrade());
                    writer.writeNext(new String[]{
                        grade.getStudent().getId_student().toString(),
                        grade.getGrade(),
                        needsRetake ? "Yes" : "No"
                    });
                }

                showAlert(Alert.AlertType.INFORMATION, "Success", "Report exported successfully to " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export report: " + e.getMessage());
            }
        }
    }

    // Helper for average grade calculation
    private double calculateAverageGrade(List<Grade> grades) {
        Map<String, Double> gradeToValue = new java.util.HashMap<>();
        gradeToValue.put("A", 4.0); gradeToValue.put("A-", 3.7); gradeToValue.put("B+", 3.3);
        gradeToValue.put("B", 3.0); gradeToValue.put("B-", 2.7); gradeToValue.put("C+", 2.3);
        gradeToValue.put("C", 2.0); gradeToValue.put("C-", 1.7); gradeToValue.put("D+", 1.3);
        gradeToValue.put("D", 1.0); gradeToValue.put("D-", 0.7); gradeToValue.put("F", 0.0);
        double sum = 0;
        int count = 0;
        for (Grade g : grades) {
            String grade = g.getGrade().trim().toUpperCase();
            if (gradeToValue.containsKey(grade)) {
                sum += gradeToValue.get(grade);
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 
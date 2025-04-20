package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.Course;
import edu.francis.my.sfupa.SQLite.Models.CourseEval;
import edu.francis.my.sfupa.SQLite.Models.SchoolYear;
import edu.francis.my.sfupa.SQLite.Models.SemesterName;
import edu.francis.my.sfupa.SQLite.Repository.CourseRepository;
import edu.francis.my.sfupa.SQLite.Repository.SchoolYearRepository;
import edu.francis.my.sfupa.SQLite.Services.CSVInstructorEval;
import edu.francis.my.sfupa.SQLite.Services.CSVGrade;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class GradebookController {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @Autowired
    private CSVInstructorEval CSVInstructorEval;

    @Autowired
    private CSVGrade csvGrade;

    @FXML
    private ComboBox<String> semesterCombo;  // For selecting the semester

    @FXML
    private CheckBox retakeCheckBox; // Checkbox for retake option

    @FXML
    private ComboBox<String> courseCombo;    // For selecting the course

    @FXML
    private ComboBox<String> courseComboo;    // For selecting the course

    @FXML
    private ComboBox<String> yearCombo;      // For selecting the year

    @FXML
    private Button uploadButton;
    @FXML
    private Button runReportButton;

    @FXML
    public void initialize() {
        if (semesterCombo != null) {
            semesterCombo.setItems(FXCollections.observableArrayList("Spring", "Summer", "Fall", "Winter"));
        }

        if (courseCombo != null) {
            courseCombo.setItems(FXCollections.observableArrayList(
                    "PA 400",
                    "PA 401",
                    "PA 402",
                    "PA 403",
                    "PA 404",
                    "PA 405",
                    "PA 406",
                    "PA 420",
                    "PA 421",
                    "PA 422",
                    "PA 423",
                    "PA 424",
                    "PA 425",
                    "PA 426",
                    "PA 427",
                    "PA 428",
                    "PA 429",
                    "PA 430",
                    "PA 431",
                    "PA 432",
                    "PA 451",
                    "PA 452",
                    "PA 453"
            ));
        }

        if (courseComboo != null) {
            courseComboo.setItems(FXCollections.observableArrayList(
                    "Evidence-Based Medicine",
                    "Introduction to U.S. Health Care",
                    "History Taking and Patient Education Skills",
                    "History Taking and Patient Education Skills Lab",
                    "Public Health",
                    "Clinical Skills",
                    "Well Child",
                    "Introduction to Medicine Module",
                    "Hematology Medicine Module",
                    "Endocrine Medicine Module",
                    "Neurology Medicine Module",
                    "Dermatology Medicine Module",
                    "Musculoskeletal Medicine Module",
                    "Eyes, Ears, Nose and Throat Medicine Module",
                    "Behavioral Medicine Module",
                    "Cardiovascular Medicine Module",
                    "Pulmonary Medicine Module",
                    "Gastrointestinal/Nutrition Medicine Module",
                    "Genitourinary Medicine Module",
                    "Reproductive Medicine Module",
                    "Didactic Clinical Experiences and Medical Documentation I",
                    "Didactic Clinical Experiences and Medical Documentation II",
                    "Didactic Comprehensive Evaluation"
            ));
        }

        if (yearCombo != null) {
            // Fetch school years from the repository
            List<SchoolYear> schoolYears = (List<SchoolYear>) schoolYearRepository.findAll();

            // Convert school year names (like "2023-2024") into a list of strings
            List<String> schoolYearNames = schoolYears.stream()
                    .map(SchoolYear::getName)  // Assuming you have getName() method for the "name" field
                    .collect(Collectors.toList());

            // Set the items for yearCombo
            yearCombo.setItems(FXCollections.observableArrayList(schoolYearNames));
        }
    }

    private File selectedFile;  // Store the chosen file

    @FXML
    private Label selectedFileLabel;  // Reference to update label in UI

    @FXML
    public void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            selectedFile = file;
            selectedFileLabel.setText(file.getName()); // Show file name in label
        } else {
            selectedFileLabel.setText("No file chosen");
        }
    }

    @FXML
    public void handleInstructorUploadEval() {
        if (selectedFile == null) {
            showAlert(Alert.AlertType.ERROR, "Please choose a file before uploading.");
            return;
        }

        // Validate selections
        if (semesterCombo.getValue() == null ||
                courseCombo.getValue() == null ||
                yearCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Please select Semester, Course, and Year before uploading.");
            return;
        }

        try {
            SemesterName selectedSemester = SemesterName.fromString(semesterCombo.getValue());
            Course selectedCourse = courseRepository.findByCourseCode(courseCombo.getValue());
            SchoolYear selectedYear = schoolYearRepository.findByName(yearCombo.getValue());
            boolean isRetake = retakeCheckBox.isSelected();

            CourseEval courseEval = CSVInstructorEval.createManualCourseEval(
                    selectedCourse.getcourseCode(),
                    (long) selectedSemester.getId(),
                    selectedYear.getIdSchoolYear()
            );

            if (courseEval == null) {
                showAlert(Alert.AlertType.ERROR, "Failed to create course evaluation. Check your inputs.");
                return;
            }

            CSVInstructorEval.processCSVFile(selectedFile, courseEval.getId());
            showAlert(Alert.AlertType.INFORMATION, "CSV uploaded and processed successfully!");

            // Reset selected file after upload
            selectedFile = null;
            selectedFileLabel.setText("No file chosen");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error processing CSV: " + e.getMessage());
        }
    }

    @FXML
    public void handleUploadGrades() {
        if (selectedFile == null) {
            showAlert(Alert.AlertType.ERROR, "Please choose a file before uploading.");
            return;
        }

        if (semesterCombo.getValue() == null || 
            courseCombo.getValue() == null || 
            yearCombo.getValue() == null) {
            showAlert(Alert.AlertType.ERROR, "Please select Semester, Course, and Year before uploading.");
            return;
        }

        try {
            SemesterName selectedSemester = SemesterName.fromString(semesterCombo.getValue());
            Course selectedCourse = courseRepository.findByCourseCode(courseCombo.getValue());
            SchoolYear selectedYear = schoolYearRepository.findByName(yearCombo.getValue());

            // Process the CSV file and count grades C and below
            int gradesBelow = csvGrade.processGradeCSV(
                selectedFile, 
                selectedCourse.getcourseCode(),
                selectedSemester.getId(),
                selectedYear.getIdSchoolYear().intValue()
            );
            
            String message = String.format("Grades uploaded successfully!\nNumber of grades C or below: %d", gradesBelow);
            showAlert(Alert.AlertType.INFORMATION, message);

            // Reset selected file after upload
            selectedFile = null;
            selectedFileLabel.setText("No file chosen");

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error processing CSV:\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(alertType == Alert.AlertType.INFORMATION ? "Success" : "Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleRunReport(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "GradeBookReport.fxml", springContext);
    }

    @FXML
    public void handleUploadCSV(ActionEvent event) throws IOException {
        // Switch to the Gradebook Upload scene
        SceneUtils.switchScene(event, "GradebookUpload.fxml", springContext);
    }

    @FXML
    public void handleCheckStudentGrades(ActionEvent event) throws IOException {
        // Switch to the Gradebook Student Grades scene
        SceneUtils.switchScene(event, "GradebookStudentGrades.fxml", springContext);
    }

    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        // Switch back to the Gradebook scene
        SceneUtils.switchScene(event, "Gradebook.fxml", springContext);
    }

    @FXML
    public void handleBackMain(ActionEvent event) throws IOException {
        // Switch back to the main view
        SceneUtils.switchScene(event, "main-view.fxml", springContext);
    }

    // --- Menu Bar Actions ---
    @FXML
    private void handleExit() {
        // Exit the application
        System.exit(0);
    }

    @FXML
    public void handleAbout(ActionEvent event) {
        // Display an informational "About" alert
        showAlert(Alert.AlertType.INFORMATION, "This application is designed to manage gradebooks, instructor evaluations, and guest lecturers.");
    }

    // --- Sidebar Navigation ---
    @FXML
    public void handleGuestLecturer(ActionEvent event) throws IOException {
        // Switch to the Course Survey scene
        SceneUtils.switchScene(event, "CourseSurvey.fxml", springContext);
    }

    @FXML
    public void handleInstructorEval(ActionEvent event) throws IOException {
        // Switch to the Instructor Evaluation scene
        SceneUtils.switchScene(event, "InstructorEval.fxml", springContext);
    }

    @FXML
    private void handleGradebook(ActionEvent event) throws IOException {
        // Switch back to the Gradebook scene
        SceneUtils.switchScene(event, "Gradebook.fxml", springContext);
    }

    @FXML
    public void handleEditGradebook(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "GradebookEditCSV.fxml", springContext);
    }

    @FXML
    public void handleEditGradesCSV(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "GradebookEditCSV.fxml", springContext);
    }
}

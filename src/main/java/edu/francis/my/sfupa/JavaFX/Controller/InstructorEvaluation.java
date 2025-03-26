package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import edu.francis.my.sfupa.SQLite.Services.CSVInstructorEval;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import edu.francis.my.sfupa.SQLite.Services.CSVInstructorEval;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class InstructorEvaluation {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @Autowired
    private CSVInstructorEval CSVInstructorEval;

    @FXML
    private ComboBox<String> semesterCombo;  // For selecting the semester

    @FXML
    private ComboBox<String> courseCombo;    // For selecting the course

    @FXML
    private ComboBox<String> yearCombo;      // For selecting the year

    @FXML
    public void initialize() {
        if (semesterCombo != null) {
            semesterCombo.setItems(FXCollections.observableArrayList("Spring", "Summer", "Fall", "Winter"));
        }

        if (courseCombo != null) {
            courseCombo.setItems(FXCollections.observableArrayList(
                    "Health 101", "Health Science 213", "Heart Studies 340", "CS101"
            ));
        }

        if (yearCombo != null) {
            // Fetch school years from the repository
            List<SchoolYear> schoolYears = (List<SchoolYear>) schoolYearRepository.findAll();  // Assuming this method exists

            // Convert school year names (like "2023-2024") into a list of strings
            List<String> schoolYearNames = schoolYears.stream()
                    .map(SchoolYear::getName)  // Assuming you have getName() method for the "name" field
                    .collect(Collectors.toList());

            // Set the items for yearCombo
            yearCombo.setItems(FXCollections.observableArrayList(schoolYearNames));
        }
    }

    public void handleInstructorUploadEval() {
        // Validate selections
        if (semesterCombo.getValue() == null ||
                courseCombo.getValue() == null ||
                yearCombo.getValue() == null) {

            showAlert("Please select Semester, Course, and Year before uploading.");
            return;
        }

        // Open file chooser for CSV
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile == null) {
            return; // User cancelled
        }

        try {
            // Find the selected semester, course, and year
            SemesterName selectedSemester = SemesterName.fromString(semesterCombo.getValue()); // This line here

            Course selectedCourse = courseRepository.findByCourseCode(courseCombo.getValue());
            System.out.println("Selected Course: " + courseCombo.getValue());
            System.out.println(selectedCourse);
            SchoolYear selectedYear = schoolYearRepository.findByName(yearCombo.getValue());

            System.out.println("Selected Year: " + yearCombo.getValue());


            // Create manual course evaluation first
            CourseEval courseEval = CSVInstructorEval.createManualCourseEval(
                    selectedCourse.getcourseCode(),
                    (long) selectedSemester.getId(), // Convert int to Long
                    selectedYear.getIdSchoolYear(),
                    "FirstName",
                    "LastName"
            );


            if (courseEval == null) {
                showAlert("Failed to create course evaluation. Check your inputs.");
                return;
            }

            // Process the CSV file
            CSVInstructorEval.processCSVFile(selectedFile, courseEval.getId());

            showAlert("CSV uploaded and processed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error processing CSV: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instructor Evaluation Upload");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    @FXML
    public void handleAnalyzeData(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEvalAnalyze.fxml", springContext);
    }
    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEval.fxml", springContext);
    }

    @FXML
    public void handleEditEval(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEvalEdit.fxml", springContext);
    }

    @FXML
    public void handleUploadEval(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEvalUpload.fxml", springContext);
    }

    @FXML
    public void handleBackMain(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "main-view.fxml", springContext);
    }

    // --- Menu Bar Actions ---
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
}

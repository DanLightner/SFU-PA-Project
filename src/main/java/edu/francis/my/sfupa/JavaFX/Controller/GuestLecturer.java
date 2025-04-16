package edu.francis.my.sfupa.JavaFX.Controller;
import edu.francis.my.sfupa.SQLite.Repository.CourseRepository;
import edu.francis.my.sfupa.SQLite.Models.Course;
import edu.francis.my.sfupa.SQLite.Models.CourseEval;
import edu.francis.my.sfupa.SQLite.Models.SchoolYear;
import edu.francis.my.sfupa.SQLite.Models.SemesterName;
import edu.francis.my.sfupa.SQLite.Models.Lecturer;
import edu.francis.my.sfupa.SQLite.Repository.SchoolYearRepository;
import edu.francis.my.sfupa.SQLite.Repository.LecturerRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import javafx.collections.FXCollections;
import edu.francis.my.sfupa.SQLite.Services.CSVInstructorEval;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.stream.StreamSupport;

@Component
public class GuestLecturer {

    @Autowired
    private edu.francis.my.sfupa.SQLite.Services.CSVInstructorEval CSVInstructorEval;

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private LecturerRepository lecturerRepository;

    @FXML
    private ComboBox<String> semesterCombo;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @FXML
    private ComboBox<String> courseCombo;

    @Autowired
    private CourseRepository courseRepository;

    @FXML
    private ComboBox<String> yearCombo;

    @FXML
    private ComboBox<String> lecturerCombo;

    @FXML
    private Label selectedFileLabel;

    private File selectedFile;

    @FXML
    public void initialize() {
        setupSemesterComboBox();
        setupCourseComboBox();
        setupYearComboBox();
        setupLecturerComboBox();
    }

    private void setupSemesterComboBox() {
        if (semesterCombo != null) {
            semesterCombo.setItems(FXCollections.observableArrayList("Spring", "Summer", "Fall", "Winter"));
        }
    }

    private void setupCourseComboBox() {
        if (courseCombo != null) {
            Iterable<Course> courses = courseRepository.findAll();
            List<String> courseOptions = new ArrayList<>();
            for (Course course : courses) {
                courseOptions.add(course.getcourseCode() + " - " + course.getName());
            }
            courseCombo.setItems(FXCollections.observableArrayList(courseOptions));
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

    private void setupLecturerComboBox() {
        if (lecturerCombo != null) {
            Iterable<Lecturer> lecturers = lecturerRepository.findAll();
            List<String> lecturerNames = new ArrayList<>();
            for (Lecturer lecturer : lecturers) {
                lecturerNames.add(lecturer.getFName() + " " + lecturer.getLName());
            }
            lecturerCombo.setItems(FXCollections.observableArrayList(lecturerNames));
        }
    }

    @FXML
    public void handleChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            selectedFile = file;
            selectedFileLabel.setText(file.getName());
        } else {
            selectedFileLabel.setText("No file chosen");
        }
    }

    @FXML
    public void handleInstructorUploadEval() {
        if (selectedFile == null) {
            showAlert("Please choose a file before uploading.");
            return;
        }

        // Validate selections
        if (lecturerCombo.getValue() == null ||
                semesterCombo.getValue() == null ||
                courseCombo.getValue() == null ||
                yearCombo.getValue() == null) {
            showAlert("Please select all fields before uploading.");
            return;
        }

        try {
            String[] lecturerName = lecturerCombo.getValue().split(" ");
            String firstName = lecturerName[0];
            String lastName = lecturerName[1];

            // Find the lecturer by name using the repository method
            Lecturer selectedLecturer = lecturerRepository.findByFirstNameAndLastName(firstName, lastName);

            if (selectedLecturer == null) {
                showAlert("Lecturer not found in the database.");
                return;
            }

            SemesterName selectedSemester = SemesterName.fromString(semesterCombo.getValue());
            String courseCode = courseCombo.getValue().split(" - ")[0];
            Course selectedCourse = courseRepository.findByCourseCode(courseCode);
            SchoolYear selectedYear = schoolYearRepository.findByName(yearCombo.getValue());

            CourseEval courseEval = CSVInstructorEval.createManualCourseEval(
                    courseCode,
                    (long) selectedSemester.getId(),
                    selectedYear.getIdSchoolYear(),
                    selectedLecturer.getId()
            );

            if (courseEval == null) {
                showAlert("Failed to create course evaluation. Check your inputs.");
                return;
            }

            CSVInstructorEval.processCSVFile(selectedFile, courseEval.getId());
            showAlert("Guest Lecturer CSV uploaded and processed successfully!");

            // Reset selected file after upload
            selectedFile = null;
            selectedFileLabel.setText("No file chosen");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error processing CSV: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gradebook Upload");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void handleRunReporting(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "GradebookReport.fxml", springContext);
    }

    @FXML
    public void handleUploadCSV(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "GradebookUpload.fxml", springContext);
    }

    @FXML
    public void handleCheckStudentGrades(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "GradebookStudentGrades.fxml", springContext);
    }

    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "CourseSurvey.fxml", springContext);
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
    private void handleGradebook(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "Gradebook.fxml", springContext);
    }

    @FXML
    public void handleInstructorEval(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEval.fxml", springContext);
    }

    @FXML
    public void handleViewGuestLecturers(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "ViewGuestLecturers.fxml", springContext);
    }

    @FXML
    public void handleAddGuestLecturer(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "AddGuestLecturerManually.fxml", springContext);
    }

    @FXML
    public void handleUploadGuestLecturer(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "UploadGuestLecturer.fxml", springContext);
    }

    @FXML
    public void handleEditGuestLecturerSurveys(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "EditGuestLecturerSurveys.fxml", springContext);
    }

    @FXML
    public void handleAnalyzeGuestLecturerSurveys(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "AnalyzeGuestLecturerSurveys.fxml", springContext);
    }
}

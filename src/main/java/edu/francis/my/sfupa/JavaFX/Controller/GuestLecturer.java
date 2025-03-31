package edu.francis.my.sfupa.JavaFX.Controller;
import edu.francis.my.sfupa.SQLite.Repository.CourseRepository;
import edu.francis.my.sfupa.SQLite.Models.Course;
import edu.francis.my.sfupa.SQLite.Models.CourseEval;
import edu.francis.my.sfupa.SQLite.Models.SchoolYear;
import edu.francis.my.sfupa.SQLite.Models.SemesterName;
import edu.francis.my.sfupa.SQLite.Repository.SchoolYearRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import java.util.stream.IntStream;

@Component
public class GuestLecturer {

    @Autowired
    private edu.francis.my.sfupa.SQLite.Services.CSVInstructorEval CSVInstructorEval;
    //CHANGE

    @Autowired
    private ApplicationContext springContext;

    @FXML
    private ComboBox<String> semesterCombo;
    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @FXML
    private ComboBox<String> courseCombo;
    @FXML
    private ComboBox<String> courseComboo;

    @Autowired
    private CourseRepository courseRepository;
    @FXML
    private ComboBox<String> yearCombo;

    @FXML
    private TextField guestLecturerField;



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
            showAlert("Please choose a file before uploading.");
            return;
        }

        // Validate selections
        if (semesterCombo.getValue() == null ||
                courseCombo.getValue() == null ||
                yearCombo.getValue() == null) {
            showAlert("Please select Semester, Course, and Year before uploading.");
            return;
        }

        try {
            SemesterName selectedSemester = SemesterName.fromString(semesterCombo.getValue());
            Course selectedCourse = courseRepository.findByCourseCode(courseCombo.getValue());
            SchoolYear selectedYear = schoolYearRepository.findByName(yearCombo.getValue());


            // Validate Guest Lecturer Name
            String guestLecturerName = guestLecturerField.getText();
            if (guestLecturerName == null || guestLecturerName.trim().isEmpty()) {
                showAlert("Please enter the Guest Lecturer's name before uploading.");
                return;
            }

            CourseEval courseEval = CSVInstructorEval.createManualCourseEval(
                    selectedCourse.getcourseCode(),
                    (long) selectedSemester.getId(),
                    selectedYear.getIdSchoolYear(),
                    "FirstName",
                    "LastName"
            );

            if (courseEval == null) {
                showAlert("Failed to create course evaluation. Check your inputs.");
                return;
            }

            CSVInstructorEval.processCSVFile(selectedFile, courseEval.getId());
            showAlert("CSV uploaded and processed successfully! Retake: ");


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
}

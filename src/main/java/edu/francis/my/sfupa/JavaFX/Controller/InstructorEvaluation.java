package edu.francis.my.sfupa.JavaFX.Controller;
import javafx.scene.control.Label;

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
import java.util.ArrayList;
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

    @Autowired
    private LecturerRepository lecturerRepository;

    @FXML
    private ComboBox<String> semesterCombo;  // For selecting the semester

    @FXML
    private ComboBox<String> courseCombo;    // For selecting the course

    @FXML
    private ComboBox<String> courseComboo;    // For selecting the course

    @FXML
    private ComboBox<String> yearCombo;      // For selecting the year

    @FXML
    private ComboBox<String> lecturerCombo;

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

        if (lecturerCombo != null) {
            // Fetch all lecturers
            Iterable<Lecturer> lecturers = lecturerRepository.findAll();
            List<String> lecturerNames = new ArrayList<>();

            // Create formatted names (FirstName LastName)
            for (Lecturer lecturer : lecturers) {
                lecturerNames.add(lecturer.getFName() + " " + lecturer.getLName());
            }

            // Set the items for lecturerCombo
            lecturerCombo.setItems(FXCollections.observableArrayList(lecturerNames));
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
                yearCombo.getValue() == null ||
                lecturerCombo.getValue() == null) {
            showAlert("Please select Semester, Course, Year, and Lecturer before uploading.");
            return;
        }

        try {
            SemesterName selectedSemester = SemesterName.fromString(semesterCombo.getValue());
            Course selectedCourse = courseRepository.findByCourseCode(courseCombo.getValue());
            SchoolYear selectedYear = schoolYearRepository.findByName(yearCombo.getValue());

            // Split the selected lecturer name into first and last name
            String[] lecturerFullName = lecturerCombo.getValue().split(" ");
            if (lecturerFullName.length < 2) {
                showAlert("Invalid lecturer name format");
                return;
            }
            String firstName = lecturerFullName[0];
            String lastName = lecturerFullName[1];

            CourseEval courseEval = CSVInstructorEval.createManualCourseEval(
                    selectedCourse.getcourseCode(),
                    (long) selectedSemester.getId(),
                    selectedYear.getIdSchoolYear(),
                    firstName,
                    lastName
            );

            if (courseEval == null) {
                showAlert("Failed to create course evaluation. Check your inputs.");
                return;
            }

            CSVInstructorEval.processCSVFile(selectedFile, courseEval.getId());
            showAlert("CSV uploaded and processed successfully!");

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
        alert.setTitle("Instructor Evaluation Upload");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // EVERYTHING BELOW IS RELATIGN TO INSTRUCTOR ANALYSIS





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

    @FXML
    public void handleViewSpecificInstructor(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEvalView.fxml", springContext);
    }

}

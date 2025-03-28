package edu.francis.my.sfupa.JavaFX.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javafx.collections.FXCollections;
import java.io.IOException;
import java.util.stream.IntStream;

@Component
public class GradebookController {

    @Autowired
    private ApplicationContext springContext;

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
                    "Health 101", "Health Science 213", "Heart Studies 340"
            ));
        }

        if (yearCombo != null) {
            yearCombo.setItems(FXCollections.observableArrayList(
                    IntStream.rangeClosed(1960, 2050)
                            .mapToObj(String::valueOf)
                            .toList()
            ));
        }
    }


    @FXML
    public void handleRunReporting(ActionEvent event) throws IOException {
        // Switch to the Gradebook Report scene
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About SFU PA");
        alert.setHeaderText("SFU PA Application");
        alert.setContentText("This application is designed to manage gradebooks, instructor evaluations, and guest lecturers.");
        alert.showAndWait();
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
}

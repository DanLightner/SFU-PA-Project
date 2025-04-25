package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.Lecturer;
import edu.francis.my.sfupa.SQLite.Repository.LecturerRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AddGuestLecturerManuallyController {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private LecturerRepository lecturerRepository;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    public void handleAddGuestLecturer() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showAlert("Please enter both first and last name.");
            return;
        }

        try {
            // Create new lecturer
            Lecturer lecturer = new Lecturer(firstName, lastName);
            lecturerRepository.save(lecturer);

            showAlert("Guest Lecturer added successfully!");
            
            // Clear fields after successful addition
            firstNameField.clear();
            lastNameField.clear();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error adding guest lecturer: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Add Guest Lecturer");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

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
} 
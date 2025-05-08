package edu.francis.my.sfupa.JavaFX.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import java.io.IOException;

@Component
public class MainController {

    @Autowired
    private ApplicationContext springContext;

    @FXML
    private void handleCourseSurvey(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "CourseSurvey.fxml", springContext);
    }

    @FXML
    private void handleInstructorEval(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEval.fxml", springContext);
    }

    @FXML
    private void handleGradebook(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "Gradebook.fxml", springContext);
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleAbout() {
        System.out.println("SFU PA Application v1.0");
    }

    @FXML
    public void handleUploadCSV(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "UploadGuestLecturer.fxml", springContext);
    }

    @FXML
    public void handleViewGuestLecturers(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "ViewGuestLecturers.fxml", springContext);
    }

    @FXML
    public void handleEditSurveys(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "EditGuestLecturerSurveys.fxml", springContext);
    }

    @FXML
    public void handleAnalyzeSurveys(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "AnalyzeGuestLecturerSurveys.fxml", springContext);
    }

    @FXML
    private ComboBox<String> semesterCombo;

    @FXML
    private ComboBox<String> courseCombo;

    @FXML
    private TextField yearField;

    @FXML
    private TextField guestLecturerField;

    @FXML
    public void handleSelectCSV(ActionEvent event) {
        System.out.println("CSV Button Clicked");
    }

    public void handleSubmitCSV(ActionEvent actionEvent) throws IOException {
        // Implement CSV handling logic here if needed
    }

    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "main-view.fxml", springContext);
    }

    @FXML
    public void handleAddSchoolYear(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "AddSchoolYear.fxml", springContext);
    }
}

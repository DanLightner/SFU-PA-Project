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

import java.io.IOException;

@Component
public class MainController {

    private Stage stage;

    @FXML
    private void handleCourseSurvey(ActionEvent event) throws IOException {
        loadScene(event, "/view/CourseSurvey.fxml");
    }

    @FXML
    private void handleInstructorEval(ActionEvent event) throws IOException {
        loadScene(event, "/view/InstructorEval.fxml");
    }

    @FXML
    private void handleGradebook(ActionEvent event) throws IOException {
        loadScene(event, "/view/Gradebook.fxml");
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
        loadScene(event, "/view/UploadGuestLecturer.fxml");
    }

    @FXML
    public void handleViewGuestLecturers(ActionEvent event) throws IOException {
        loadScene(event, "/view/ViewGuestLecturers.fxml");
    }

    @FXML
    public void handleEditSurveys(ActionEvent event) throws IOException {
        loadScene(event, "/view/EditGuestLecturerSurveys.fxml");
    }

    @FXML
    public void handleAnalyzeSurveys(ActionEvent event) throws IOException {
        loadScene(event, "/view/AnalyzeGuestLecturerSurveys.fxml");
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

    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }



    private void loadScene(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

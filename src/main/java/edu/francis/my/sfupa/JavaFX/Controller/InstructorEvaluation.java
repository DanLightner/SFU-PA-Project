package edu.francis.my.sfupa.JavaFX.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class InstructorEvaluation {

    @Autowired
    private ApplicationContext springContext;

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

package edu.francis.my.sfupa.JavaFX.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GuestLecturer{

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void handleRunReporting(ActionEvent event) throws IOException {
        switchScene(event, "GradebookReport.fxml");
    }

    @FXML
    public void handleUploadCSV(ActionEvent event) throws IOException {
        switchScene(event, "GradebookUpload.fxml");
    }

    @FXML
    public void handleCheckStudentGrades(ActionEvent event) throws IOException {
        switchScene(event, "GradebookStudentGrades.fxml");
    }

    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        switchScene(event, "CourseSurvey.fxml");
    }
    @FXML
    public void handleBackMain(ActionEvent event) throws IOException {
        switchScene(event, "main-view.fxml");
    }

}

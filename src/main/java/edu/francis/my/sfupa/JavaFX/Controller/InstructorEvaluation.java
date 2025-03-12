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
public class InstructorEvaluation {

    private void switchScene(ActionEvent event, String fxmlFile) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/" + fxmlFile));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    public void handleAnalyzeData(ActionEvent event) throws IOException {
        switchScene(event, "InstructorEvalAnalyze.fxml");
    }

    @FXML
    public void handleEditEval(ActionEvent event) throws IOException {
        switchScene(event, "InstructorEvalEdit.fxml");
    }

    @FXML
    public void handleUploadEval(ActionEvent event) throws IOException {
        switchScene(event, "InstructorEvalUpload.fxml");
    }
    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        switchScene(event, "InstructorEval.fxml");
    }
    @FXML
    public void handleBackMain(ActionEvent event) throws IOException {
        switchScene(event, "main-view.fxml");
    }
}

package edu.francis.my.sfupa.JavaFX.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
    public void handleEditEval(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEvalEdit.fxml", springContext);
    }

    @FXML
    public void handleUploadEval(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEvalUpload.fxml", springContext);
    }

    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEval.fxml", springContext);
    }

    @FXML
    public void handleBackMain(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "main-view.fxml", springContext);
    }
}

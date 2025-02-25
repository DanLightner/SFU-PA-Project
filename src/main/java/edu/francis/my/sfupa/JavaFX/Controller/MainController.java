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
    public void handleBack(ActionEvent event) throws IOException {
        loadScene(event, "/view/main-view.fxml");
    }


    private void loadScene(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}

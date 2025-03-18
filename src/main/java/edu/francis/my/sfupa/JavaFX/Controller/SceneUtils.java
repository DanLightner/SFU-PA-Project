package edu.francis.my.sfupa.JavaFX.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

public class SceneUtils {

    /**
     * Switch to a new scene while applying the global stylesheet
     *
     * @param event The action event triggering the scene change
     * @param fxmlFile The FXML file to load
     * @param springContext The Spring application context for dependency injection
     * @throws IOException If the FXML file cannot be loaded
     */
    public static void switchScene(ActionEvent event, String fxmlFile, ApplicationContext springContext) throws IOException {
        // Create FXMLLoader
        FXMLLoader loader = new FXMLLoader(SceneUtils.class.getResource("/view/" + fxmlFile));

        // Set controller factory for Spring integration
        if (springContext != null) {
            loader.setControllerFactory(springContext::getBean);
        }

        // Load the FXML
        Parent root = loader.load();

        // Get current stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Create new scene with stylesheet
        Scene scene = new Scene(root);
        scene.getStylesheets().add(SceneUtils.class.getResource("/styles/sfu-theme.css").toExternalForm());

        // Set and show the scene
        stage.setScene(scene);
        stage.show();
    }
}
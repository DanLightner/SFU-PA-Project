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
     * Switch to a new scene while maintaining fullscreen and window size.
     *
     * @param event The action event triggering the scene change.
     * @param fxmlFile The FXML file to load.
     * @param springContext The Spring application context for dependency injection.
     * @throws IOException If the FXML file cannot be loaded.
     */
    public static void switchScene(ActionEvent event, String fxmlFile, ApplicationContext springContext) throws IOException {
        // Get the current stage
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        // Save fullscreen state and window size before switching scenes
        boolean wasFullScreen = stage.isFullScreen();
        double width = stage.getWidth();
        double height = stage.getHeight();

        // Create FXMLLoader
        FXMLLoader loader = new FXMLLoader(SceneUtils.class.getResource("/view/" + fxmlFile));

        // Set controller factory for Spring integration
        if (springContext != null) {
            loader.setControllerFactory(springContext::getBean);
        }

        // Load the FXML
        Parent root = loader.load();

        // Create new scene with stylesheet
        Scene scene = new Scene(root);
        scene.getStylesheets().add(SceneUtils.class.getResource("/styles/sfu-theme.css").toExternalForm());

        // Set the scene and maintain size
        stage.setScene(scene);
        stage.setWidth(width);
        stage.setHeight(height);

        // Restore fullscreen mode
        stage.setFullScreen(wasFullScreen);

        stage.show();
    }
}

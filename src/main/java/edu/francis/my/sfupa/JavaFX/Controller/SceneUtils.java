package edu.francis.my.sfupa.JavaFX.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.URL;

public class SceneUtils {

    public static void switchScene(ActionEvent event, String fxmlFile, ApplicationContext springContext) throws IOException {
        try {
            // Try multiple ways to load the resource
            URL fxmlUrl = null;
            
            // Try the regular class resource loading first
            fxmlUrl = SceneUtils.class.getResource("/view/" + fxmlFile);
            
            // If that fails, try the context classloader
            if (fxmlUrl == null) {
                fxmlUrl = Thread.currentThread().getContextClassLoader().getResource("view/" + fxmlFile);
            }
            
            // If that still fails, try without the leading slash
            if (fxmlUrl == null) {
                fxmlUrl = SceneUtils.class.getResource("view/" + fxmlFile);
            }
            
            if (fxmlUrl == null) {
                throw new IOException("Could not find FXML resource: " + fxmlFile);
            }

            // Create FXMLLoader with the found URL
            FXMLLoader loader = new FXMLLoader(fxmlUrl);

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
            
            // Try to load the stylesheet in multiple ways
            URL cssUrl = SceneUtils.class.getResource("/styles/sfu-theme.css");
            if (cssUrl == null) {
                cssUrl = Thread.currentThread().getContextClassLoader().getResource("styles/sfu-theme.css");
            }
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            // Set and show the scene
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception e) {
            System.err.println("Error switching scene to " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to switch scene: " + e.getMessage(), e);
        }
    }
}
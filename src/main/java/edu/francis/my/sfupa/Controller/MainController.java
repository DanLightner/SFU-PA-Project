package edu.francis.my.sfupa.Controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import org.springframework.stereotype.Component;

@Component
public class MainController {

    public void handleExit(ActionEvent event) {
        System.exit(0);
    }

    public void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("SFU PA Application");
        alert.setContentText("This is a JavaFX + Spring Boot application for our project.");
        alert.showAndWait();
    }
}

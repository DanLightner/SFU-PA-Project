package edu.francis.my.sfupa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class HelloFXApplication extends Application {

    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Start the Spring Boot application context
        springContext = SpringApplication.run(SfuPaProjectApplication.class);
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Load FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));



        // Ensure dependency injection works with Spring
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 600, 400);

        stage.setScene(scene);
        stage.setTitle("Spring Boot + JavaFX");
        stage.show();
    }

    @Override
    public void stop() {
        // Close the Spring application context when the JavaFX application stops
        //test
        springContext.close();
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }
}

package edu.francis.my.sfupa;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class HelloFXApplication extends Application {

    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        // Start the Spring Boot application context
        springContext = SpringApplication.run(SfuPaProjectApplication.class);
    }

    @Override
    public void start(Stage stage) {
        // Create the JavaFX scene
        Label helloLabel = new Label("Hello World, this is a simple application made from JavaFX & Spring Boot!");
        StackPane root = new StackPane(helloLabel);
        Scene scene = new Scene(root, 400, 200);

        // Set the scene on the stage and show it
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

package edu.francis.my.sfupa;

import edu.francis.my.sfupa.SQLite.Services.CSVInstructorEval;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.Objects;

public class HelloFXApplication extends Application {

    private static ConfigurableApplicationContext springContext;


    @Override
    public void init() {
        // Start the Spring Boot application context
        springContext = SpringApplication.run(SfuPaProjectApplication.class);

        try {
            // Retrieve the bean from the Spring context
            CSVInstructorEval csvInstructorEval = springContext.getBean(CSVInstructorEval.class);
            csvInstructorEval.runHardcodedImport();
            System.out.println("CSV Import did work");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("CSV Import did NOT work");
        }

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
        springContext.close();
    }

    public static void main(String[] args) {
        // Launch the JavaFX application
        launch(args);
    }
}

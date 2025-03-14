package edu.francis.my.sfupa;

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
        // Get ConnectDB bean from Spring
        /*
        database = springContext.getBean(ConnectDB.class);

        if (database.getConnection() != null) {
            System.out.println("Database connection established successfully!");
        } else {
            System.out.println("Failed to connect to the database.");
        }
        */

    }

    @Override
    public void start(Stage stage) throws IOException {
        // Load FXML file
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/main-view.fxml"));



        // Ensure dependency injection works with Spring
        fxmlLoader.setControllerFactory(springContext::getBean);

        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 600, 400);
        //scene.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());



        stage.setScene(scene);
        stage.setTitle("Spring Boot + JavaFX");
        stage.show();
    }

    @Override
    public void stop() {
        // Close the Spring application context when the JavaFX application stops
        //test
        springContext.close();
        /*
        if (database != null) {
            System.out.println("Database connection closed successfully!");
            database.closeConnection();  // Ensure the connection is properly closed
        }

         */
    }

    public static void main(String[] args) {
        // Launch the JavaFX application

        launch(args);
    }
}

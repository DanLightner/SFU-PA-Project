package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.SchoolYear;
import edu.francis.my.sfupa.SQLite.Repository.SchoolYearRepository;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class SchoolYearController {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @FXML
    private TextField schoolYearField;

    @FXML
    private ComboBox<String> existingYearsCombo;

    @FXML
    public void initialize() {
        loadExistingSchoolYears();
    }

    private void loadExistingSchoolYears() {
        List<String> schoolYears = StreamSupport.stream(schoolYearRepository.findAll().spliterator(), false)
                .map(SchoolYear::getName)
                .collect(Collectors.toList());
        
        existingYearsCombo.setItems(FXCollections.observableArrayList(schoolYears));
    }

    @FXML
    public void handleSaveSchoolYear() {
        String schoolYearName = schoolYearField.getText().trim();
        
        if (!isValidSchoolYearFormat(schoolYearName)) {
            showAlert(Alert.AlertType.ERROR, "Invalid Format", 
                    "Please enter a valid school year in the format YYYY-YYYY (e.g., 2025-2026)");
            return;
        }
        
        // Check if school year already exists
        if (schoolYearExists(schoolYearName)) {
            showAlert(Alert.AlertType.ERROR, "Duplicate Entry", 
                    "This school year already exists in the database.");
            return;
        }
        
        // Save the new school year
        SchoolYear newSchoolYear = new SchoolYear(schoolYearName);
        schoolYearRepository.save(newSchoolYear);
        
        // Refresh the list
        loadExistingSchoolYears();
        
        // Clear the field
        schoolYearField.clear();
        
        showAlert(Alert.AlertType.INFORMATION, "Success", 
                "School year " + schoolYearName + " has been added successfully.");
    }
    
    private boolean isValidSchoolYearFormat(String schoolYear) {
        // Check format: YYYY-YYYY where both years are 4 digits
        Pattern pattern = Pattern.compile("^\\d{4}-\\d{4}$");
        if (!pattern.matcher(schoolYear).matches()) {
            return false;
        }
        
        // Check that the second year is one more than the first
        String[] years = schoolYear.split("-");
        int firstYear = Integer.parseInt(years[0]);
        int secondYear = Integer.parseInt(years[1]);
        
        return secondYear == firstYear + 1;
    }
    
    private boolean schoolYearExists(String schoolYearName) {
        return StreamSupport.stream(schoolYearRepository.findAll().spliterator(), false)
                .anyMatch(year -> year.getName().equals(schoolYearName));
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation methods
    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "main-view.fxml", springContext);
    }

    @FXML
    public void handleCourseSurvey(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "CourseSurvey.fxml", springContext);
    }

    @FXML
    public void handleGradebook(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "Gradebook.fxml", springContext);
    }

    @FXML
    public void handleInstructorEval(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEval.fxml", springContext);
    }

    @FXML
    public void handleExit() {
        System.exit(0);
    }

    @FXML
    public void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About SFU PA");
        alert.setHeaderText("SFU PA Application");
        alert.setContentText("This application is designed to manage PA program data including school years.");
        alert.showAndWait();
    }
}

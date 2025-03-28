package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.CourseEval;
import edu.francis.my.sfupa.SQLite.Models.Classes;
import edu.francis.my.sfupa.SQLite.Models.Lecturer;
import edu.francis.my.sfupa.SQLite.Services.CourseEvalService;
import edu.francis.my.sfupa.SQLite.Repository.ClassesRepository;
import edu.francis.my.sfupa.SQLite.Repository.LecturerRepository;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.opencsv.CSVReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class CourseEvalCsvImporter {

    @Autowired
    private CourseEvalService courseEvalService;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    // Method to prompt the user for a CSV file and import it
    public void importCsv(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            processCsvFile(selectedFile);
        } else {
            System.out.println("No file selected.");
        }
    }

    // Method to process the selected CSV file and import data into the database
    private void processCsvFile(File file) {
        List<CourseEval> courseEvalList = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            reader.readNext(); // Skip header row

            // Process each row in the CSV
            while ((line = reader.readNext()) != null) {
                // Assuming CSV format is: ID, Course ID, Lecturer ID
                Integer courseId = Integer.parseInt(line[1]);  // Course ID in the second column
                Integer lecturerId = Integer.parseInt(line[2]);  // Lecturer ID in the third column

                // Fetch related course and lecturer from the database
                Classes course = classesRepository.findById(courseId).orElse(null);
                Lecturer lecturer = lecturerRepository.findById(lecturerId).orElse(null);

                if (course != null && lecturer != null) {
                    // Create a new CourseEval entity
                    CourseEval courseEval = new CourseEval();
                    courseEval.setCourse(course);
                    courseEval.setLecturer(lecturer);

                    // Save the CourseEval record to the database
                    courseEvalService.save(courseEval);
                } else {
                    System.out.println("Skipping row: Course or Lecturer not found for Course ID: " + courseId + " and Lecturer ID: " + lecturerId);
                }
            }

            System.out.println("CSV Data Imported Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error importing CSV: " + e.getMessage());
        }
    }
}

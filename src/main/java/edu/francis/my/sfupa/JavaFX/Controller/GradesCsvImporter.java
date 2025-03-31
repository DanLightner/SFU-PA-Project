package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.Classes;
import edu.francis.my.sfupa.SQLite.Models.Student;
import edu.francis.my.sfupa.SQLite.Models.Grade;
import edu.francis.my.sfupa.SQLite.Repository.ClassesRepository;
import edu.francis.my.sfupa.SQLite.Repository.StudentRepository;
import edu.francis.my.sfupa.SQLite.Repository.GradeRepository;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class GradesCsvImporter {

    private GradeRepository gradeRepository;
    private ClassesRepository classesRepository;
    private StudentRepository studentRepository;

    // Constructor to initialize repositories
    public GradesCsvImporter(GradeRepository gradeRepository, ClassesRepository classesRepository, StudentRepository studentRepository) {
        this.gradeRepository = gradeRepository;
        this.classesRepository = classesRepository;
        this.studentRepository = studentRepository;
    }

    // Method to prompt the user for a CSV file and import it
    public void importCsv(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Grades CSV File");
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
        List<Grade> gradeList = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] line;
            reader.readNext(); // Skip header row (if present)

            // Process each row in the CSV
            while ((line = reader.readNext()) != null) {
                // Assuming CSV format is: retake, Class_id_class, id_grade, student_id_student, grade
                Boolean retake = Integer.parseInt(line[0]) == 1;  // Binary (0 or 1), 1 means true (retake)
                Integer classId = Integer.parseInt(line[1]);  // Class ID (corresponds to the course)
                Integer gradeId = Integer.parseInt(line[2]);  // Grade ID (if needed to reference a grade type)
                Integer studentId = Integer.parseInt(line[3]);  // Student ID
                String grade = line[4];  // Grade (e.g., A, B+, etc.)

                // Fetch related class and student from the database
                Classes course = classesRepository.findById(classId).orElse(null);
                Student student = studentRepository.findById(studentId).orElse(null);

                if (course != null && student != null) {
                    // Create a new Grade entity
                    Grade gradeEntity = new Grade();
                    gradeEntity.setRetake(retake);  // Set the retake flag
                    gradeEntity.setStudent(student);  // Set the related student
                    gradeEntity.setStudentClass(course);  // Set the related class (studentClass)
                    gradeEntity.setGrade(grade);  // Set the grade value

                    // Add to the list for bulk saving later
                    gradeList.add(gradeEntity);
                } else {
                    System.out.println("Skipping row: Course or Student not found for Class ID: " + classId + ", Student ID: " + studentId);
                }
            }

            // Save all Grade records to the database
            gradeRepository.saveAll(gradeList);
            System.out.println("CSV Data Imported Successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error importing CSV: " + e.getMessage());
        }
    }
}

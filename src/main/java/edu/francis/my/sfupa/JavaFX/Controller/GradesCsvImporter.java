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

            int rowNumber = 1;
            while ((line = reader.readNext()) != null) {
                rowNumber++;
                
                // Skip empty rows
                if (line.length == 0 || (line.length == 1 && line[0].trim().isEmpty())) {
                    continue;
                }

                // Validate that we have all required fields
                if (line.length < 5 || isAnyFieldEmpty(line)) {
                    System.out.println("Skipping row " + rowNumber + ": Missing required fields");
                    continue;
                }

                try {
                    Boolean retake = Integer.parseInt(line[0].trim()) == 1;
                    Integer classId = Integer.parseInt(line[1].trim());
                    Integer gradeId = Integer.parseInt(line[2].trim());
                    Long studentId = Long.parseLong(line[3].trim());
                    String grade = line[4].trim();

                    // Validate student ID and grade
                    if (studentId == 0 || grade.isEmpty()) {
                        System.out.println("Skipping row " + rowNumber + ": Empty student ID or grade");
                        continue;
                    }

                    // First check if student exists
                    Student student = studentRepository.findById(studentId).orElse(null);
                    Classes course = classesRepository.findById(classId).orElse(null);

                    if (course == null) {
                        System.out.println("Skipping row " + rowNumber + ": Course not found with ID: " + classId);
                        continue;
                    }

                    // If student doesn't exist, create new student
                    if (student == null) {
                        student = new Student();
                        student.setId_student(studentId);  // Using the correct field name id_student
                        try {
                            student = studentRepository.save(student);
                            System.out.println("Created new student with ID: " + student.getId_student());
                        } catch (Exception e) {
                            System.out.println("Error creating student in row " + rowNumber + ": " + e.getMessage());
                            continue;
                        }
                    }

                    // Create grade entry
                    Grade gradeEntity = new Grade();
                    gradeEntity.setRetake(retake);
                    gradeEntity.setStudent(student);
                    gradeEntity.setStudentClass(course);
                    gradeEntity.setGrade(grade);
                    gradeList.add(gradeEntity);

                } catch (NumberFormatException e) {
                    System.out.println("Skipping row " + rowNumber + ": Invalid number format - " + e.getMessage());
                    continue;
                }
            }

            // Save all valid grade records to the database
            if (!gradeList.isEmpty()) {
                try {
                    gradeRepository.saveAll(gradeList);
                    System.out.println("Successfully imported " + gradeList.size() + " grades!");
                } catch (Exception e) {
                    System.out.println("Error saving grades: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("No valid data found to import.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error importing CSV: " + e.getMessage());
        }
    }

    // Helper method to check if any field in the row is empty
    private boolean isAnyFieldEmpty(String[] line) {
        for (String field : line) {
            if (field == null || field.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}

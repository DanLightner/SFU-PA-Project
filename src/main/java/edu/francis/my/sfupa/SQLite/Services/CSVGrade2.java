package edu.francis.my.sfupa.SQLite.Services;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CSVGrade2 {
/*
    @Autowired
    private StudentRepository StudentRepository;

    @Autowired
    private GradeRepository GradeRepository;

    @Autowired
    private ClassesRepository classesRepository;


    @Autowired
    private CourseRepository CourseRepository;

    @Autowired
    private SemesterRepository SemesterRepository;

    // Hardcoded file path for testing - maintain existing path logic
    private static final String DEFAULT_FILE_PATH = System.getProperty("user.home") + "/Downloads/Example of Instructor Eval.csv";

    /**
     * Process grades from a CSV file using Apache Commons CSV
     * @param filePath Path to the CSV file, or null to use default path
     * @return List of created Grade objects
     */

    /*
    public List<Grade> processGradesFromCSV(String filePath) {
        String path = (filePath != null) ? filePath : DEFAULT_FILE_PATH;
        List<Grade> createdGrades = new ArrayList<>();

        try (Reader reader = new FileReader(path);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .withIgnoreHeaderCase()
                     .withTrim())) {

            for (CSVRecord record : csvParser) {
                try {
                    Long studentId = Long.parseLong(record.get("Student ID"));
                    String courseCode = record.get("Course Code");
                    String semesterName = record.get("Semester");
                    String year = (record.get("Year"));
                    String gradeValue = record.get("Grade");
                    boolean retake = Boolean.parseBoolean(record.get("Retake"));

                    Grade grade = createGradeFromCSVData(studentId, courseCode, semesterName, year, gradeValue, retake);
                    if (grade != null) {
                        createdGrades.add(grade);
                    }
                } catch (Exception e) {
                    System.out.println("Error processing record: " + record.toString());
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return createdGrades;
    }

    /**
     * Creates a Grade entry based on CSV data
     */

    /*
    public Grade createGradeFromCSVData(Long studentId, String courseCode, String semesterName,
                                        String year, String gradeValue, boolean retake) {
        try {
            String name="Course";
            // Find student
            Optional<Student> studentOpt = StudentRepository.findById(studentId);
            if (studentOpt.isEmpty()) {
                System.out.println("Student not found with ID: " + studentId);
                return null;
            }

            // Find course
            Course course = findCourse(courseCode,name);
            if (course.equals(null)) {
                System.out.println("Course not found: " + courseCode);
                return null;
            }

            // Find semester by name
            Semester semester = findSemester(semesterName);
            if (semester.equals(null)) {
                System.out.println("Semester not found: " + semesterName);
                return null;
            }

            // Find or create class
            Classes classEntity = findOrCreateClass(courseOpt.get(), semesterOpt.get(), year);
            if (classEntity == null) {
                return null;
            }

            // Check if grade already exists
            Optional<Grade> existingGrade = GradeRepository.findByStudentAndStudentClass(
                    studentOpt.get(), classEntity);

            if (existingGrade.isPresent()) {
                // Update existing grade
                Grade grade = existingGrade.get();
                grade.setGrade(gradeValue);
                grade.setRetake(retake);
                return GradeRepository.save(grade);
            } else {
                // Create new grade
                Grade grade = new Grade(
                        studentOpt.get(),
                        classEntity,
                        gradeValue,
                        retake
                );
                return GradeRepository.save(grade);
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid semester name: " + semesterName);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Finds an existing class or creates a new one
     */

    /*
    private Classes findOrCreateClass(Course course, Semester semester, int year) {
        Optional<Classes> classOpt = ClassesRepository.findByCourseAndSemesterAndYear(
                course, semester, year);

        if (classOpt.isPresent()) {
            return classOpt.get();
        } else {
            Classes newClass = new Classes();
            newClass.setClassCode(course);
            newClass.setSemester(semester);
            newClass.setSchoolYear(year);
            return ClassesRepository.save(newClass);
        }
    }

    /**
     * JavaFX method to select CSV file and process grades
     */

    /*
    public List<Grade> selectAndProcessGradeCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Grade CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            return processGradesFromCSV(selectedFile.getAbsolutePath());
        }

        return Collections.emptyList();
    }
    private Course findCourse(String courseCode, String name) {
        // This is a simplified approach - in a real application, you'd want more robust logic
        // to avoid duplicate entries
        Iterable<Course> courses = CourseRepository.findAll();
        for (Course course : courses) {
            if (course.getcourseCode().equalsIgnoreCase(courseCode)) {
                return course;
            }
        }
        return null;

    }
    private Semester findSemester(String name) {
        // This is a simplified approach - in a real application, you'd want more robust logic
        // to avoid duplicate entries
        Iterable<Semester> semesters = SemesterRepository.findAll();
        for (Semester semester : semesters) {
            if (semester.getName().equals(name)) {
                return semester;
            }
        }
        return null;

    }

    */
}
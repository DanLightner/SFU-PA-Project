package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GradebookExport {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private ApplicationContext springContext;

    @FXML private ComboBox<String> courseCmb;
    @FXML private ComboBox<String> yearCmb;
    @FXML private ComboBox<String> semesterCmb;

    @FXML
    public void initialize() {
        setupCourseComboBox();
        setupYearComboBox();
        setupSemesterComboBox();

    }
    private void setupCourseComboBox() {
        if (courseCmb != null) {
            List<Course> courses = new ArrayList<>();
            courseRepository.findAll().forEach(courses::add);
            List<String> courseCodes = courses.stream()
                    .map(Course::getcourseCode)
                    .collect(Collectors.toList());
            courseCmb.setItems(FXCollections.observableArrayList(courseCodes));
        }
    }

    private void setupYearComboBox() {
        if (yearCmb != null) {
            List<SchoolYear> schoolYears = new ArrayList<>();
            schoolYearRepository.findAll().forEach(schoolYears::add);
            List<String> yearNames = schoolYears.stream()
                    .map(SchoolYear::getName)
                    .collect(Collectors.toList());
            yearCmb.setItems(FXCollections.observableArrayList(yearNames));
        }
    }

    private void setupSemesterComboBox() {
        if (semesterCmb != null) {
            semesterCmb.setItems(FXCollections.observableArrayList("Spring", "Summer", "Fall", "Winter"));
        }
    }

    //export all grades for selceted class
    @FXML
    public void exportReport() throws IOException {
        if (courseCmb.getValue() == null || yearCmb.getValue() == null || semesterCmb.getValue() == null) {
            showAlert("Please select Course, Year, and Semester");
            return;
        }

        try {
            // Get selected values
            String courseCode = courseCmb.getValue();
            String yearName = yearCmb.getValue();
            String semesterName = semesterCmb.getValue();

            // Find the class
            Course course = courseRepository.findByCourseCode(courseCode);
            SchoolYear year = schoolYearRepository.findByName(yearName);
            SemesterName semesterEnum = SemesterName.fromString(semesterName);
            Semester semester = semesterRepository.findById(semesterEnum.getId()).orElse(null);

            if (course == null || year == null || semester == null) {
                showAlert("Could not find matching course, year, or semester");
                return;
            }

            Classes classEntity = classesRepository.findByClassCode_CourseCodeAndSemester_IdAndSchoolYear_IdSchoolYear(
                            courseCode, semesterEnum.getId(), year.getIdSchoolYear().intValue())
                    .orElse(null);
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Grades Report");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV files", "*.csv"));
            File file = fileChooser.showSaveDialog(courseCmb.getScene().getWindow());

            if (file == null) return;

            List<Grade> grades = gradeRepository.findByStudentClass(classEntity);

            if (grades.isEmpty()) {
                showAlert("No grades to export for the selected course.");
                return;
            }

            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("All Grades Report");
                writer.printf("Course: %s, Year: %s, Semester: %s%n%n", courseCode, yearName, semesterName);
                writer.println("Student ID,Grade");

                StudentInClassRep(grades,writer);
                StudentsRetake(grades,writer);

                showAlert("All grades exported successfully to:\n" + file.getAbsolutePath());
            }

        }
        catch (Exception e) {
            e.printStackTrace();
            showAlert("Error generating report: " + e.getMessage());
        }
    }
    private void StudentInClassRep(List<Grade> grades,PrintWriter writer)
    {
        for (Grade grade : grades) {
            String studentId = grade.getStudent().getId_student().toString();
            String studentGrade = grade.getGrade();
            writer.printf("%s,%s,%s%n", studentId, studentGrade);
        }
    }
    private void StudentsRetake(List<Grade> grades,PrintWriter writer)
    {
        for (Grade grade : grades) {
            String studentId = grade.getStudent().getId_student().toString();
            String studentGrade = grade.getGrade();
            Boolean retake=grade.isRetake();
            if(retake) {
                writer.printf("%s,%s,%s%n", studentId, studentGrade);
            }
        }
    }
    private void SudentsLowGrade(List<Grade> grades,PrintWriter writer)
    {
        int ng=0; //all grades
        int lg=0; //lowe grades
        for (Grade grade : grades) {
            ng++;
            String studentId = grade.getStudent().getId_student().toString();
            String studentGrade = grade.getGrade();
            studentGrade=studentGrade.trim().toUpperCase();
            if(studentGrade.startsWith("C") || studentGrade.equals("D+") || studentGrade.equals("D") ||
                    studentGrade.equals("D-") || studentGrade.equals("F")) {
                lg++;
                writer.printf("%s,%s,%s%n", studentId, studentGrade);
            }
        }
        double lowerGradePercentage = 0;
        if (ng > 0) {
            lowerGradePercentage = (double) lg / ng * 100;
        }

        writer.printf("Percentage of lower grades in class: %.2f%%%n", lowerGradePercentage);
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Gradebook Report");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}

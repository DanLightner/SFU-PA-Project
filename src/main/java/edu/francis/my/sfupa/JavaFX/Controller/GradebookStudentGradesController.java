package edu.francis.my.sfupa.JavaFX.Controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import edu.francis.my.sfupa.JavaFX.Controller.SceneUtils;
import java.io.IOException;
import org.springframework.context.ApplicationContext;
import javafx.event.ActionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import edu.francis.my.sfupa.SQLite.Models.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

@Component
public class GradebookStudentGradesController {
    @Autowired
    private ApplicationContext applicationContext;

    @PersistenceContext
    private EntityManager entityManager;

    @FXML private TextField studentIdField;
    @FXML private Label studentNameLabel;
    @FXML private Label studentIdLabel;
    @FXML private Label studentProgramLabel;
    @FXML private Label gpaLabel;
    @FXML private Label totalCreditsLabel;

    @FXML private TableView<CourseGrade> gradesTable;
    @FXML private TableColumn<CourseGrade, String> courseCodeColumn;
    @FXML private TableColumn<CourseGrade, String> courseNameColumn;
    @FXML private TableColumn<CourseGrade, String> semesterColumn;
    @FXML private TableColumn<CourseGrade, String> gradeColumn;
    @FXML private TableColumn<CourseGrade, Integer> creditsColumn;

    @FXML private TableView<CourseGrade> lowGradesTable;
    @FXML private TableColumn<CourseGrade, String> lowGradeCourseCodeColumn;
    @FXML private TableColumn<CourseGrade, String> lowGradeCourseNameColumn;
    @FXML private TableColumn<CourseGrade, String> lowGradeSemesterColumn;
    @FXML private TableColumn<CourseGrade, String> lowGradeColumn;

    private ObservableList<CourseGrade> courseGrades = FXCollections.observableArrayList();
    private ObservableList<CourseGrade> lowGrades = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize main grades table
        courseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        gradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        creditsColumn.setCellValueFactory(new PropertyValueFactory<>("credits"));
        gradesTable.setItems(courseGrades);

        // Initialize low grades table
        lowGradeCourseCodeColumn.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        lowGradeCourseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        lowGradeSemesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        lowGradeColumn.setCellValueFactory(new PropertyValueFactory<>("grade"));
        lowGradesTable.setItems(lowGrades);

        // Add style for low grades
        lowGradeColumn.setCellFactory(column -> new TableCell<CourseGrade, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: red;");
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        String studentId = studentIdField.getText().trim();

        if (studentId.isEmpty()) {
            showAlert("Error", "Please enter a student ID");
            return;
        }

        try {
            // Find student
            TypedQuery<Student> studentQuery = entityManager.createQuery(
                    "SELECT s FROM Student s WHERE s.id_student = :studentId", Student.class);
            studentQuery.setParameter("studentId", Long.parseLong(studentId));
            List<Student> students = studentQuery.getResultList();

            if (!students.isEmpty()) {
                Student student = students.get(0);
                studentNameLabel.setText(student.getId_student().toString());
                studentIdLabel.setText(studentId);
                studentProgramLabel.setText("Student");  // Add program field to Student entity if needed

                // Get all grades for the student
                TypedQuery<Grade> gradesQuery = entityManager.createQuery(
                        "SELECT g FROM Grade g " +
                                "JOIN FETCH g.studentClass c " +
                                "JOIN FETCH c.classCode " +
                                "JOIN FETCH c.semester " +
                                "JOIN FETCH c.schoolYear " +
                                "WHERE g.student.id_student = :studentId", Grade.class);
                gradesQuery.setParameter("studentId", Long.parseLong(studentId));
                List<Grade> grades = gradesQuery.getResultList();

                courseGrades.clear();
                lowGrades.clear();  // Clear low grades table
                double totalGradePoints = 0.0;
                int totalCredits = 0;

                for (Grade grade : grades) {
                    Classes studentClass = grade.getStudentClass();
                    Course course = studentClass.getClassCode();
                    String semesterInfo = studentClass.getSemester().getName().toString() + " " +
                            studentClass.getSchoolYear().getName();

                    CourseGrade courseGrade = new CourseGrade(
                            course.getcourseCode(),
                            course.getName(),
                            semesterInfo,
                            grade.getGrade(),
                            3  // Add credits field to Course entity if needed
                    );

                    courseGrades.add(courseGrade);

                    // Check if grade is C or below
                    String gradeValue = grade.getGrade().toUpperCase();
                    if (gradeValue.startsWith("C") || gradeValue.startsWith("D") || gradeValue.equals("F")) {
                        lowGrades.add(courseGrade);
                    }

                    double gradePoints = convertGradeToPoints(grade.getGrade());
                    totalGradePoints += gradePoints * 3;
                    totalCredits += 3;
                }

                // Sort low grades by grade (worst first)
                lowGrades.sort((g1, g2) -> {
                    double points1 = convertGradeToPoints(g1.getGrade());
                    double points2 = convertGradeToPoints(g2.getGrade());
                    return Double.compare(points1, points2);
                });

                // Calculate and display GPA
                double gpa = totalCredits > 0 ? totalGradePoints / totalCredits : 0.0;
                gpaLabel.setText(String.format("%.2f", gpa));
                totalCreditsLabel.setText(String.valueOf(totalCredits));

            } else {
                showAlert("Not Found", "Student ID not found");
                clearStudentData();
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid student ID format");
            clearStudentData();
        } catch (Exception e) {
            showAlert("Database Error", "Error accessing database: " + e.getMessage());
            clearStudentData();
        }
    }

    private double convertGradeToPoints(String grade) {
        return switch (grade.toUpperCase()) {
            case "A" -> 4.0;
            case "A-" -> 3.7;
            case "B+" -> 3.3;
            case "B" -> 3.0;
            case "B-" -> 2.7;
            case "C+" -> 2.3;
            case "C" -> 2.0;
            case "C-" -> 1.7;
            case "D+" -> 1.3;
            case "D" -> 1.0;
            case "F" -> 0.0;
            default -> 0.0;
        };
    }

    private void clearStudentData() {
        studentNameLabel.setText("-");
        studentIdLabel.setText("-");
        studentProgramLabel.setText("-");
        gpaLabel.setText("-");
        totalCreditsLabel.setText("-");
        courseGrades.clear();
        lowGrades.clear();  // Clear low grades table
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) studentIdField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleAbout() {
        showAlert("About", "Student Grades Lookup System\nVersion 1.0");
    }

    @FXML
    private void handleBackMain(ActionEvent event) {
        try {
            SceneUtils.switchScene(event, "/view/Main.fxml", applicationContext);
        } catch (IOException e) {
            showAlert("Error", "Failed to switch to main screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleGuestLecturer(ActionEvent event) {
        try {
            SceneUtils.switchScene(event, "/view/GuestLecturer.fxml", applicationContext);
        } catch (IOException e) {
            showAlert("Error", "Failed to switch to guest lecturer screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleGradebook(ActionEvent event) {
        try {
            SceneUtils.switchScene(event, "/view/Gradebook.fxml", applicationContext);
        } catch (IOException e) {
            showAlert("Error", "Failed to switch to gradebook screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleInstructorEval(ActionEvent event) {
        try {
            SceneUtils.switchScene(event, "/view/InstructorEval.fxml", applicationContext);
        } catch (IOException e) {
            showAlert("Error", "Failed to switch to instructor evaluation screen: " + e.getMessage());
        }
    }

    // Model class for course grades
    public static class CourseGrade {
        private final String courseCode;
        private final String courseName;
        private final String semester;
        private final String grade;
        private final int credits;

        public CourseGrade(String courseCode, String courseName, String semester, String grade, int credits) {
            this.courseCode = courseCode;
            this.courseName = courseName;
            this.semester = semester;
            this.grade = grade;
            this.credits = credits;
        }

        public String getCourseCode() { return courseCode; }
        public String getCourseName() { return courseName; }
        public String getSemester() { return semester; }
        public String getGrade() { return grade; }
        public int getCredits() { return credits; }
    }
}
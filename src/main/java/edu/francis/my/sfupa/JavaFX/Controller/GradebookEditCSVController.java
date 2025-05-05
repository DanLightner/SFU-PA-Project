package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.Grade;
import edu.francis.my.sfupa.SQLite.Models.Course;
import edu.francis.my.sfupa.SQLite.Models.Classes;
import edu.francis.my.sfupa.SQLite.Models.Semester;
import edu.francis.my.sfupa.SQLite.Models.SchoolYear;
import edu.francis.my.sfupa.SQLite.Repository.GradeRepository;
import edu.francis.my.sfupa.SQLite.Repository.CourseRepository;
import edu.francis.my.sfupa.SQLite.Repository.ClassesRepository;
import edu.francis.my.sfupa.SQLite.Repository.SemesterRepository;
import edu.francis.my.sfupa.SQLite.Repository.SchoolYearRepository;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.util.Callback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Collections;

@Component
public class GradebookEditCSVController {
    @Autowired private ApplicationContext springContext;
    @Autowired private GradeRepository gradeRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ClassesRepository classesRepository;
    @Autowired private SemesterRepository semesterRepository;
    @Autowired private SchoolYearRepository schoolYearRepository;

    @FXML private ComboBox<String> semesterCombo;
    @FXML private ComboBox<String> yearCombo;
    @FXML private ComboBox<String> courseCombo;
    @FXML private TableView<GradeRow> gradeTable;
    @FXML private TableColumn<GradeRow, String> studentIdColumn;
    @FXML private TableColumn<GradeRow, String> gradeColumn;
    @FXML private Button saveButton;

    private ObservableList<GradeRow> gradeRows = FXCollections.observableArrayList();
    private static final List<String> VALID_GRADES = Arrays.asList(
            "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"
    );

    @FXML
    public void initialize() {
        setupCourseComboBox();
        setupSemesterComboBox();
        setupYearComboBox();
        setupTableColumns();
        // Add listener for dynamic year filtering
        courseCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateYearOptions(newVal);
            }
        });
        // Add listener for dynamic semester filtering
        yearCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && courseCombo.getValue() != null) {
                updateSemesterOptions(courseCombo.getValue(), newVal);
            }
        });
    }

    private void setupCourseComboBox() {
        if (courseCombo != null) {
            List<Course> courses = StreamSupport.stream(courseRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList());
            
            List<String> courseOptions = courses.stream()
                    .map(course -> course.getcourseCode() + " - " + course.getName())
                    .collect(Collectors.toList());

            // Sort by PA number
            Collections.sort(courseOptions, (a, b) -> {
                String numA = a.replaceAll("\\D+", "");
                String numB = b.replaceAll("\\D+", "");
                return Integer.compare(Integer.parseInt(numA), Integer.parseInt(numB));
            });

            courseCombo.setItems(FXCollections.observableArrayList(courseOptions));
        }
    }

    private void setupSemesterComboBox() {
        if (semesterCombo != null) {
            semesterCombo.setItems(FXCollections.observableArrayList("Spring", "Summer", "Fall", "Winter"));
        }
    }

    private void setupYearComboBox() {
        if (yearCombo != null) {
            List<SchoolYear> schoolYears = (List<SchoolYear>) schoolYearRepository.findAll();
            List<String> schoolYearNames = schoolYears.stream()
                    .map(SchoolYear::getName)
                    .collect(Collectors.toList());
            yearCombo.setItems(FXCollections.observableArrayList(schoolYearNames));
        }
    }

    private void setupTableColumns() {
        studentIdColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStudentId()));
        gradeColumn.setCellValueFactory(data -> data.getValue().gradeProperty());
        gradeColumn.setCellFactory(ComboBoxTableCell.forTableColumn(FXCollections.observableArrayList(VALID_GRADES)));
        gradeColumn.setOnEditCommit(event -> {
            String newGrade = event.getNewValue();
            if (VALID_GRADES.contains(newGrade)) {
                event.getRowValue().setGrade(newGrade);
            } else {
                showAlert("Invalid Grade", "Please enter a valid grade (A, A-, B+, etc.)");
                event.getTableView().refresh();
            }
        });
        gradeTable.setEditable(true);
        gradeTable.setItems(gradeRows);
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        String semester = semesterCombo.getValue();
        String year = yearCombo.getValue();
        String courseComboValue = courseCombo.getValue();
        if (courseComboValue == null || year == null || semester == null) {
            showAlert("Missing Selection", "Please select course, year, and semester.");
            return;
        }
        // Extract course code from the combined string (format: 'PA400 - Course Name')
        String courseCode = courseComboValue.split(" - ")[0];
        // Find the Classes entity for the selected semester, year, and course
        List<Classes> classesList = StreamSupport.stream(classesRepository.findAll().spliterator(), false)
                .filter(c -> c.getClassCode().getcourseCode().equals(courseCode)
                        && c.getSemester().getName().toString().equalsIgnoreCase(semester)
                        && c.getSchoolYear().getName().equals(year))
                .collect(Collectors.toList());
        gradeRows.clear();
        for (Classes classes : classesList) {
            List<Grade> grades = StreamSupport.stream(gradeRepository.findAll().spliterator(), false)
                    .filter(g -> g.getStudentClass().getIdClass().equals(classes.getIdClass()))
                    .collect(Collectors.toList());
            for (Grade grade : grades) {
                gradeRows.add(new GradeRow(grade));
            }
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        for (GradeRow row : gradeRows) {
            if (!VALID_GRADES.contains(row.getGrade())) {
                showAlert("Invalid Grade", "Invalid grade found: " + row.getGrade());
                return;
            }
        }
        // Save all changes
        for (GradeRow row : gradeRows) {
            Grade grade = row.getGradeEntity();
            grade.setGrade(row.getGrade());
            gradeRepository.save(grade);
        }
        showAlert("Success", "Grades updated successfully.");
    }

    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "Gradebook.fxml", springContext);
    }

    // --- Navigation and Menu Bar Methods ---
    @FXML
    public void handleBackMain(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "main-view.fxml", springContext);
    }

    @FXML
    public void handleGuestLecturer(ActionEvent event) throws IOException {
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
    private void handleExit() {
        System.exit(0);
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About SFU PA");
        alert.setHeaderText("SFU PA Application");
        alert.setContentText("This application is designed to manage gradebooks, instructor evaluations, and guest lecturers.");
        alert.showAndWait();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void updateYearOptions(String selectedCourse) {
        String courseCode = selectedCourse.split(" - ")[0];
        List<Classes> classes = StreamSupport.stream(classesRepository.findAll().spliterator(), false)
                .filter(cls -> cls.getClassCode() != null &&
                        cls.getClassCode().getcourseCode().equals(courseCode))
                .collect(Collectors.toList());
        List<String> years = classes.stream()
                .map(cls -> cls.getSchoolYear().getName())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        yearCombo.setItems(FXCollections.observableArrayList(years));
        yearCombo.getSelectionModel().clearSelection();
        semesterCombo.getSelectionModel().clearSelection();
        semesterCombo.setItems(FXCollections.observableArrayList());
    }

    private void updateSemesterOptions(String selectedCourse, String selectedYear) {
        String courseCode = selectedCourse.split(" - ")[0];
        List<Classes> filteredClasses = StreamSupport.stream(classesRepository.findAll().spliterator(), false)
                .filter(cls -> cls.getClassCode() != null &&
                        cls.getClassCode().getcourseCode().equals(courseCode) &&
                        cls.getSchoolYear().getName().equals(selectedYear))
                .collect(Collectors.toList());
        List<String> semesters = filteredClasses.stream()
                .map(cls -> cls.getSemester().getName().name())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        semesterCombo.setItems(FXCollections.observableArrayList(semesters));
        semesterCombo.getSelectionModel().clearSelection();
    }

    public static class GradeRow {
        private final Grade gradeEntity;
        private final SimpleStringProperty studentId;
        private final SimpleStringProperty grade;
        public GradeRow(Grade gradeEntity) {
            this.gradeEntity = gradeEntity;
            this.studentId = new SimpleStringProperty(gradeEntity.getStudent().getId_student().toString());
            this.grade = new SimpleStringProperty(gradeEntity.getGrade());
        }
        public String getStudentId() { return studentId.get(); }
        public SimpleStringProperty gradeProperty() { return grade; }
        public String getGrade() { return grade.get(); }
        public void setGrade(String value) { grade.set(value); }
        public Grade getGradeEntity() { return gradeEntity; }
    }
} 
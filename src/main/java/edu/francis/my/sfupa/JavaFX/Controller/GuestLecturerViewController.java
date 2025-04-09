package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class GuestLecturerViewController {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    @Autowired
    private CourseRepository courseRepository;

    @FXML
    private ComboBox<String> lecturerCombo;

    @FXML
    private TableView<GuestLecturerData> evaluationTable;

    @FXML
    private TableColumn<GuestLecturerData, String> courseIdColumn;

    @FXML
    private TableColumn<GuestLecturerData, String> courseNameColumn;

    @FXML
    private TableColumn<GuestLecturerData, String> semesterColumn;

    @FXML
    private TableColumn<GuestLecturerData, String> yearColumn;

    @FXML
    public void initialize() {
        setupLecturerComboBox();
        setupTableView();
    }

    private void setupLecturerComboBox() {
        Iterable<Lecturer> lecturers = lecturerRepository.findAll();
        List<String> lecturerNames = new ArrayList<>();

        for (Lecturer lecturer : lecturers) {
            lecturerNames.add(lecturer.getFName() + " " + lecturer.getLName());
        }

        lecturerCombo.setItems(FXCollections.observableArrayList(lecturerNames));
        
        lecturerCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateEvaluationTable(newVal);
            }
        });
    }

    private void setupTableView() {
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
    }

    private void updateEvaluationTable(String lecturerName) {
        String[] nameParts = lecturerName.split(" ");
        if (nameParts.length < 2) return;

        String firstName = nameParts[0];
        String lastName = nameParts[1];

        // Find the lecturer
        Lecturer lecturer = findLecturerByName(firstName, lastName);
        if (lecturer == null) return;

        // Get all course evaluations for this lecturer
        Iterable<CourseEval> allEvals = courseEvalRepository.findAll();
        List<CourseEval> courseEvals = new ArrayList<>();
        for (CourseEval eval : allEvals) {
            if (eval.getLecturer().getFName().equals(lecturer.getFName()) && 
                eval.getLecturer().getLName().equals(lecturer.getLName())) {
                courseEvals.add(eval);
            }
        }
        
        ObservableList<GuestLecturerData> data = FXCollections.observableArrayList();
        
        for (CourseEval eval : courseEvals) {
            Classes classEntity = eval.getCourse();
            Course course = classEntity.getClassCode();
            Semester semester = classEntity.getSemester();
            SchoolYear year = classEntity.getSchoolYear();

            data.add(new GuestLecturerData(
                course.getcourseCode(),
                course.getName(),
                semester.getName(),
                year.getName()
            ));
        }

        evaluationTable.setItems(data);
    }

    private Lecturer findLecturerByName(String firstName, String lastName) {
        Iterable<Lecturer> lecturers = lecturerRepository.findAll();
        for (Lecturer lecturer : lecturers) {
            if (lecturer.getFName().equals(firstName) && lecturer.getLName().equals(lastName)) {
                return lecturer;
            }
        }
        return null;
    }

    // Navigation methods
    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "CourseSurvey.fxml", springContext);
    }

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
    public void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About SFU PA");
        alert.setHeaderText("SFU PA Application");
        alert.setContentText("This application is designed to manage gradebooks, instructor evaluations, and guest lecturers.");
        alert.showAndWait();
    }

    @FXML
    public void handleAddGuestLecturer(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "AddGuestLecturerManually.fxml", springContext);
    }
} 
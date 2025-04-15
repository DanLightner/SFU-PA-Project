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

@Controller
public class GuestLecturerViewController {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    @Autowired
    private CourseRepository courseRepository;

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
        setupTableView();
        loadAllEvaluations();
    }

    private void setupTableView() {
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
    }

    private void loadAllEvaluations() {
        ObservableList<GuestLecturerData> data = FXCollections.observableArrayList();
        
        Iterable<CourseEval> courseEvals = courseEvalRepository.findAll();
        for (CourseEval eval : courseEvals) {
            Classes classEntity = eval.getCourse();
            Course course = classEntity.getClassCode();
            Semester semester = classEntity.getSemester();
            SchoolYear year = classEntity.getSchoolYear();

            data.add(new GuestLecturerData(
                course.getcourseCode(),
                course.getName(),
                semester.getName().toString(),
                year.getName()
            ));
        }

        evaluationTable.setItems(data);
    }

    // Navigation methods
    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "GuestLecturer.fxml", springContext);
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
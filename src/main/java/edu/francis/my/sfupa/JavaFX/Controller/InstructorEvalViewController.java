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
import java.util.stream.StreamSupport;

@Controller
public class InstructorEvalViewController {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    @Autowired
    private CourseRepository courseRepository;

    @FXML
    private TableView<InstructorEvalData> evaluationTable;

    @FXML
    private TableColumn<InstructorEvalData, String> courseIdColumn;

    @FXML
    private TableColumn<InstructorEvalData, String> courseNameColumn;

    @FXML
    private TableColumn<InstructorEvalData, String> semesterColumn;

    @FXML
    private TableColumn<InstructorEvalData, String> yearColumn;

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
        ObservableList<InstructorEvalData> data = FXCollections.observableArrayList();
        
        // Filter for instructor evaluations only
        List<CourseEval> courseEvals = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
            .filter(eval -> eval.getEvalType() == CourseEval.EvalType.INSTRUCTOR)
            .collect(Collectors.toList());

        for (CourseEval eval : courseEvals) {
            if (eval.getCourse() != null) {
                Classes classEntity = eval.getCourse();
                Course course = classEntity.getClassCode();
                Semester semester = classEntity.getSemester();
                SchoolYear year = classEntity.getSchoolYear();

                data.add(new InstructorEvalData(
                    course.getcourseCode(),
                    course.getName(),
                    semester.getName(),
                    year.getName()
                ));
            }
        }

        evaluationTable.setItems(data);
    }

    // Navigation methods
    @FXML
    public void handleBack(ActionEvent event) throws IOException {
        SceneUtils.switchScene(event, "InstructorEval.fxml", springContext);
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
} 
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
public class GuestLecturerViewController {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

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
    private ComboBox<String> lecturerCombo;

    @FXML
    private ComboBox<String> semesterFilterCombo;

    @FXML
    private ComboBox<String> yearFilterCombo;

    @FXML
    public void initialize() {
        setupTableView();
        setupComboBoxes();
        
        // Add listeners for filtering
        lecturerCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateTableForSelectedLecturer();
            }
        });

        semesterFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateTableForSelectedLecturer();
        });

        yearFilterCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateTableForSelectedLecturer();
        });
    }

    private void setupComboBoxes() {
        // Setup lecturer combo
        List<Lecturer> lecturers = StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        List<String> lecturerNames = lecturers.stream()
                .filter(l -> l != null && l.getFName() != null && l.getLName() != null)
                .map(l -> l.getFName() + " " + l.getLName())
                .collect(Collectors.toList());
        lecturerCombo.setItems(FXCollections.observableArrayList(lecturerNames));

        // Setup semester filter
        semesterFilterCombo.setItems(FXCollections.observableArrayList("All", "Spring", "Summer", "Fall", "Winter"));
        semesterFilterCombo.setValue("All");

        // Setup year filter with null checks
        List<String> years = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
                .filter(eval -> eval != null && 
                        eval.getCourse() != null && 
                        eval.getCourse().getSchoolYear() != null &&
                        eval.getCourse().getSchoolYear().getName() != null)
                .map(eval -> eval.getCourse().getSchoolYear().getName())
                .distinct()
                .collect(Collectors.toList());
        years.add(0, "All");
        yearFilterCombo.setItems(FXCollections.observableArrayList(years));
        yearFilterCombo.setValue("All");
    }

    private void setupTableView() {
        courseIdColumn.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        semesterColumn.setCellValueFactory(new PropertyValueFactory<>("semester"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("year"));
    }

    private void updateTableForSelectedLecturer() {
        if (lecturerCombo.getValue() == null) {
            evaluationTable.getItems().clear();
            return;
        }

        String[] lecturerName = lecturerCombo.getValue().split(" ");
        String firstName = lecturerName[0];
        String lastName = lecturerName[1];

        // Find the lecturer
        Lecturer selectedLecturer = StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
                .filter(l -> l.getFName().equals(firstName) && l.getLName().equals(lastName))
                .findFirst()
                .orElse(null);

        if (selectedLecturer == null) {
            return;
        }

        // Get all valid course evaluations for this lecturer using the new method
        Iterable<CourseEval> lecturerEvals = courseEvalRepository.findValidEvaluationsByLecturerId(selectedLecturer.getId());

        // Apply filters
        String selectedSemester = semesterFilterCombo.getValue();
        String selectedYear = yearFilterCombo.getValue();

        ObservableList<GuestLecturerData> filteredData = FXCollections.observableArrayList();
        
        for (CourseEval eval : lecturerEvals) {
            try {
                Classes classEntity = eval.getCourse();
                Course course = classEntity.getClassCode();
                String semester = classEntity.getSemester().getName().toString();
                String year = classEntity.getSchoolYear().getName();

                // Apply filters
                if ((selectedSemester.equals("All") || semester.equals(selectedSemester)) &&
                    (selectedYear.equals("All") || year.equals(selectedYear))) {
                    
                    filteredData.add(new GuestLecturerData(
                        course.getcourseCode(),
                        course.getName(),
                        semester,
                        year
                    ));
                }
            } catch (Exception e) {
                // Skip this entry if there's any error accessing the data
                continue;
            }
        }

        evaluationTable.setItems(filteredData);
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
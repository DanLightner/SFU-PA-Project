package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class EditGuestLecturerSurveysController {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @Autowired
    private ResponseLikertRepository responseLikertRepository;

    @Autowired
    private ResponseOpenRepository responseOpenRepository;

    @FXML
    private ComboBox<String> lecturerCombo;

    @FXML
    private ComboBox<String> courseCombo;

    @FXML
    private ComboBox<String> semesterCombo;

    @FXML
    private ComboBox<String> yearCombo;

    @FXML
    private TableView<CourseEval> evalTable;

    @FXML
    private TableColumn<CourseEval, String> courseColumn;

    @FXML
    private TableColumn<CourseEval, String> instructorColumn;

    @FXML
    private TableColumn<CourseEval, String> semesterColumn;

    @FXML
    private TableColumn<CourseEval, String> yearColumn;

    @FXML
    private TableView<ResponseData> responsesTable;

    @FXML
    private TableColumn<ResponseData, String> questionColumn;

    @FXML
    private TableColumn<ResponseData, String> responseColumn;

    @FXML
    private TableColumn<ResponseData, String> responseTypeColumn;

    @FXML
    private ComboBox<String> editLecturerCombo;

    @FXML
    private ComboBox<String> editCourseCombo;

    @FXML
    private ComboBox<String> editSemesterCombo;

    @FXML
    private ComboBox<String> editYearCombo;

    private CourseEval selectedEvaluation;

    @FXML
    public void initialize() {
        responsesTable.setEditable(true);
        setupLecturerComboBox();
        setupTableColumns();

        // Add listeners for cascading filtering
        lecturerCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateCourseOptionsForLecturer(newVal);
                courseCombo.getSelectionModel().clearSelection();
                yearCombo.getSelectionModel().clearSelection();
                semesterCombo.getSelectionModel().clearSelection();
                courseCombo.setDisable(false);
                yearCombo.setDisable(true);
                semesterCombo.setDisable(true);
            }
        });
        courseCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && lecturerCombo.getValue() != null) {
                updateYearOptionsForLecturerAndCourse(lecturerCombo.getValue(), newVal);
                yearCombo.getSelectionModel().clearSelection();
                semesterCombo.getSelectionModel().clearSelection();
                yearCombo.setDisable(false);
                semesterCombo.setDisable(true);
            }
        });
        yearCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && lecturerCombo.getValue() != null && courseCombo.getValue() != null) {
                updateSemesterOptionsForLecturerCourseYear(lecturerCombo.getValue(), courseCombo.getValue(), newVal);
                semesterCombo.getSelectionModel().clearSelection();
                semesterCombo.setDisable(false);
            }
        });
        semesterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && lecturerCombo.getValue() != null && courseCombo.getValue() != null && yearCombo.getValue() != null) {
                handleSearch(null);
            }
        });

        // Initially disable all but lecturer
        courseCombo.setDisable(true);
        yearCombo.setDisable(true);
        semesterCombo.setDisable(true);

        // Initialize edit combo boxes with the same data
        editLecturerCombo.setItems(lecturerCombo.getItems());
        editCourseCombo.setItems(courseCombo.getItems());
        editSemesterCombo.setItems(semesterCombo.getItems());
        editYearCombo.setItems(yearCombo.getItems());
    }

    private void setupTableColumns() {
        courseColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(cellData.getValue().getCourse().getClassCode().getcourseCode());
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
        
        instructorColumn.setCellValueFactory(cellData -> {
            try {
                Lecturer lecturer = cellData.getValue().getLecturer();
                return new SimpleStringProperty(lecturer != null ? 
                    lecturer.getFName() + " " + lecturer.getLName() : "N/A");
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
        
        semesterColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(cellData.getValue().getCourse().getSemester().getName().name());
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });
        
        yearColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(cellData.getValue().getCourse().getSchoolYear().getName());
            } catch (Exception e) {
                return new SimpleStringProperty("Error");
            }
        });

        // Setup response table columns
        questionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQuestion()));
        responseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResponse()));
        responseTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
    }

    private void setupLecturerComboBox() {
        List<String> lecturers = StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
            .map(l -> l.getFName() + " " + l.getLName())
            .collect(Collectors.toList());
        lecturerCombo.setItems(FXCollections.observableArrayList(lecturers));
    }

    private void updateCourseOptionsForLecturer(String lecturerName) {
        String[] nameParts = lecturerName.split(" ");
        if (nameParts.length < 2) return;
        String firstName = nameParts[0];
        String lastName = nameParts[1];
        Lecturer lecturer = StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
            .filter(l -> l.getFName().equals(firstName) && l.getLName().equals(lastName))
            .findFirst().orElse(null);
        if (lecturer == null) {
            courseCombo.setItems(FXCollections.observableArrayList());
            return;
        }
        List<String> courses = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
            .filter(eval -> eval.getLecturer() != null && eval.getLecturer().getId().equals(lecturer.getId()))
            .map(eval -> eval.getCourse().getClassCode().getcourseCode() + " - " + eval.getCourse().getClassCode().getName())
            .distinct()
            .collect(Collectors.toList());
        courseCombo.setItems(FXCollections.observableArrayList(courses));
    }

    private void updateYearOptionsForLecturerAndCourse(String lecturerName, String courseWithName) {
        String[] nameParts = lecturerName.split(" ");
        if (nameParts.length < 2) return;
        String firstName = nameParts[0];
        String lastName = nameParts[1];
        String courseCode = courseWithName.split(" - ")[0];
        Lecturer lecturer = StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
            .filter(l -> l.getFName().equals(firstName) && l.getLName().equals(lastName))
            .findFirst().orElse(null);
        if (lecturer == null) {
            yearCombo.setItems(FXCollections.observableArrayList());
            return;
        }
        List<String> years = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
            .filter(eval -> eval.getLecturer() != null && eval.getLecturer().getId().equals(lecturer.getId()))
            .filter(eval -> eval.getCourse() != null && eval.getCourse().getClassCode() != null && eval.getCourse().getClassCode().getcourseCode().equals(courseCode))
            .map(eval -> eval.getCourse().getSchoolYear().getName())
            .distinct()
            .collect(Collectors.toList());
        yearCombo.setItems(FXCollections.observableArrayList(years));
    }

    private void updateSemesterOptionsForLecturerCourseYear(String lecturerName, String courseWithName, String year) {
        String[] nameParts = lecturerName.split(" ");
        if (nameParts.length < 2) return;
        String firstName = nameParts[0];
        String lastName = nameParts[1];
        String courseCode = courseWithName.split(" - ")[0];
        Lecturer lecturer = StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
            .filter(l -> l.getFName().equals(firstName) && l.getLName().equals(lastName))
            .findFirst().orElse(null);
        if (lecturer == null) {
            semesterCombo.setItems(FXCollections.observableArrayList());
            return;
        }
        List<String> semesters = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
            .filter(eval -> eval.getLecturer() != null && eval.getLecturer().getId().equals(lecturer.getId()))
            .filter(eval -> eval.getCourse() != null && eval.getCourse().getClassCode() != null && eval.getCourse().getClassCode().getcourseCode().equals(courseCode))
            .filter(eval -> eval.getCourse().getSchoolYear() != null && eval.getCourse().getSchoolYear().getName().equals(year))
            .map(eval -> eval.getCourse().getSemester().getName().name())
            .distinct()
            .collect(Collectors.toList());
        semesterCombo.setItems(FXCollections.observableArrayList(semesters));
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        String lecturerName = lecturerCombo.getValue();
        String courseCode = courseCombo.getValue() != null ? courseCombo.getValue().split(" - ")[0] : null;
        String semester = semesterCombo.getValue();
        String year = yearCombo.getValue();

        if (lecturerName == null || courseCode == null || semester == null || year == null) {
            showAlert("Please select all search criteria");
            return;
        }

        try {
            String[] nameParts = lecturerName.split(" ");
            if (nameParts.length < 2) {
                showAlert("Invalid lecturer name format");
                return;
            }

            String firstName = nameParts[0];
            String lastName = nameParts[1];

            // Find the lecturer
            Lecturer lecturer = StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
                .filter(l -> l.getFName().equals(firstName) && l.getLName().equals(lastName))
                .findFirst()
                .orElse(null);

            if (lecturer == null) {
                showAlert("Lecturer not found");
                return;
            }

            // Get all evaluations for this lecturer and course
            List<CourseEval> evaluations = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
                .filter(eval -> {
                    try {
                        return eval.getLecturer() != null && 
                               eval.getLecturer().getId().equals(lecturer.getId()) &&
                               eval.getCourse() != null &&
                               eval.getCourse().getClassCode() != null &&
                               eval.getCourse().getClassCode().getcourseCode().equals(courseCode) &&
                               eval.getCourse().getSemester() != null &&
                               eval.getCourse().getSemester().getName().name().equals(semester) &&
                               eval.getCourse().getSchoolYear() != null &&
                               eval.getCourse().getSchoolYear().getName().equals(year);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

            if (evaluations.isEmpty()) {
                showAlert("No evaluations found matching the selected criteria");
                evalTable.setItems(FXCollections.observableArrayList());
                responsesTable.setItems(FXCollections.observableArrayList());
                return;
            }

            evalTable.setItems(FXCollections.observableArrayList(evaluations));
        } catch (Exception e) {
            showAlert("Error during search: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEvalSelection(MouseEvent event) {
        selectedEvaluation = evalTable.getSelectionModel().getSelectedItem();
        if (selectedEvaluation != null) {
            // Populate edit fields
            Lecturer lecturer = selectedEvaluation.getLecturer();
            if (lecturer != null) {
                editLecturerCombo.setValue(lecturer.getFName() + " " + lecturer.getLName());
            }
            
            Classes classes = selectedEvaluation.getCourse();
            if (classes != null) {
                editCourseCombo.setValue(classes.getClassCode().getcourseCode() + " - " + classes.getClassCode().getName());
                editSemesterCombo.setValue(classes.getSemester().getName().name());
                editYearCombo.setValue(classes.getSchoolYear().getName());
            }

            // Load responses
            loadResponses(selectedEvaluation);
        }
    }

    private void loadResponses(CourseEval evaluation) {
        List<ResponseData> responses = new ArrayList<>();
        
        // Load Likert responses
        StreamSupport.stream(responseLikertRepository.findAll().spliterator(), false)
            .filter(r -> r.getCourseEval().getId().equals(evaluation.getId()))
            .forEach(r -> responses.add(new ResponseData(
                r.getQuestion().getText(),
                r.getResponse(),
                "Likert",
                r.getIdResponse(),
                true
            )));

        // Load Open responses
        StreamSupport.stream(responseOpenRepository.findAll().spliterator(), false)
            .filter(r -> r.getCourseEval().getId().equals(evaluation.getId()))
            .forEach(r -> responses.add(new ResponseData(
                r.getQuestion().getText(),
                r.getResponse(),
                "Open",
                r.getIdResponse(),
                false
            )));

        responsesTable.setItems(FXCollections.observableArrayList(responses));

        // Make response column editable
        responseColumn.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return object;
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        }));

        responseColumn.setOnEditCommit(event -> {
            ResponseData responseData = event.getRowValue();
            String newValue = event.getNewValue();
            
            try {
                if (responseData.isLikert) {
                    // For Likert responses, validate that it's a number between 1 and 5
                    int value = Integer.parseInt(newValue);
                    if (value < 1 || value > 5) {
                        showAlert("Likert responses must be between 1 and 5");
                        responsesTable.refresh();
                        return;
                    }
                    
                    Optional<ResponseLikert> response = StreamSupport.stream(responseLikertRepository.findAll().spliterator(), false)
                        .filter(r -> r.getIdResponse().equals(responseData.id))
                        .findFirst();
                    
                    if (response.isPresent()) {
                        ResponseLikert likertResponse = response.get();
                        likertResponse.setResponse(newValue);
                        responseLikertRepository.save(likertResponse);
                        showAlert("Response updated successfully");
                    }
                } else {
                    Optional<ResponseOpen> response = StreamSupport.stream(responseOpenRepository.findAll().spliterator(), false)
                        .filter(r -> r.getIdResponse().equals(responseData.id))
                        .findFirst();
                    
                    if (response.isPresent()) {
                        ResponseOpen openResponse = response.get();
                        openResponse.setResponse(newValue);
                        responseOpenRepository.save(openResponse);
                        showAlert("Response updated successfully");
                    }
                }
                
                responseData.response = newValue;
            } catch (NumberFormatException e) {
                if (responseData.isLikert) {
                    showAlert("Likert responses must be numeric values between 1 and 5");
                    responsesTable.refresh();
                }
            } catch (Exception e) {
                showAlert("Error saving response: " + e.getMessage());
                responsesTable.refresh();
            }
        });
    }

    @FXML
    public void handleSaveChanges(ActionEvent event) {
        if (selectedEvaluation == null) {
            showAlert("Please select an evaluation to edit");
            return;
        }

        try {
            // Update instructor
            String[] instructorName = editLecturerCombo.getValue().split(" ");
            List<Lecturer> lecturers = StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
                .filter(l -> l.getFName().equals(instructorName[0]) && l.getLName().equals(instructorName[1]))
                .collect(Collectors.toList());
            
            if (!lecturers.isEmpty()) {
                selectedEvaluation.setLecturer(lecturers.get(0));
            }

            // Update course details
            Classes classes = selectedEvaluation.getCourse();
            String courseCode = editCourseCombo.getValue().split(" - ")[0];
            Course course = StreamSupport.stream(courseRepository.findAll().spliterator(), false)
                .filter(c -> c.getcourseCode().equals(courseCode))
                .findFirst()
                .orElse(null);
            
            if (course != null) {
                classes.setClassCode(course);
            }

            // Save changes
            courseEvalRepository.save(selectedEvaluation);
            showAlert("Changes saved successfully");

            // Refresh the table
            handleSearch(null);
        } catch (Exception e) {
            showAlert("Error saving changes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper class for responses table
    private static class ResponseData {
        private final String question;
        private String response;
        private final String type;
        private final Long id;
        private final boolean isLikert;

        public ResponseData(String question, String response, String type, Long id, boolean isLikert) {
            this.question = question;
            this.response = response;
            this.type = type;
            this.id = id;
            this.isLikert = isLikert;
        }

        public String getQuestion() { return question; }
        public String getResponse() { return response; }
        public String getType() { return type; }
    }

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
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About SFU PA");
        alert.setHeaderText("SFU PA Application");
        alert.setContentText("This application is designed to manage gradebooks, instructor evaluations, and guest lecturers.");
        alert.showAndWait();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Guest Lecturer Survey");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
public class InstructorEvalEditController {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @Autowired
    private ResponseLikertRepository responseLikertRepository;

    @Autowired
    private ResponseOpenRepository responseOpenRepository;

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
    private TableColumn<CourseEval, String> semesterColumn;

    @FXML
    private TableColumn<CourseEval, String> yearColumn;

    @FXML
    private ComboBox<String> editCourseCombo;

    @FXML
    private ComboBox<String> editSemesterCombo;

    @FXML
    private ComboBox<String> editYearCombo;

    @FXML
    private TableView<ResponseData> responsesTable;

    @FXML
    private TableColumn<ResponseData, String> questionColumn;

    @FXML
    private TableColumn<ResponseData, String> responseColumn;

    @FXML
    private TableColumn<ResponseData, String> responseTypeColumn;

    private CourseEval selectedEvaluation;

    @FXML
    public void initialize() {
        System.out.println("Initializing InstructorEvalEditController...");
        responsesTable.setEditable(true);
        System.out.println("CourseEvalRepository: " + (courseEvalRepository != null ? "injected" : "null"));
        System.out.println("CourseRepository: " + (courseRepository != null ? "injected" : "null"));
        System.out.println("SchoolYearRepository: " + (schoolYearRepository != null ? "injected" : "null"));

        // Initialize course combo box
        List<Course> courses = StreamSupport.stream(courseRepository.findAll().spliterator(), false)
            .collect(Collectors.toList());
        List<String> courseOptions = courses.stream()
            .map(course -> course.getcourseCode() + " - " + course.getName())
            .collect(Collectors.toList());
        Collections.sort(courseOptions, (a, b) -> {
            String numA = a.replaceAll("\\D+", "");
            String numB = b.replaceAll("\\D+", "");
            return Integer.compare(Integer.parseInt(numA), Integer.parseInt(numB));
        });
        System.out.println("Available courses: " + courseOptions);
        courseCombo.setItems(FXCollections.observableArrayList(courseOptions));

        // Disable year and semester initially
        yearCombo.setDisable(true);
        semesterCombo.setDisable(true);

        // Add listeners for cascading filtering
        courseCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateYearOptionsForCourse(newVal);
                yearCombo.getSelectionModel().clearSelection();
                semesterCombo.getSelectionModel().clearSelection();
                yearCombo.setDisable(false);
                semesterCombo.setDisable(true);
            }
        });
        yearCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && courseCombo.getValue() != null) {
                updateSemesterOptionsForCourseYear(courseCombo.getValue(), newVal);
                semesterCombo.getSelectionModel().clearSelection();
                semesterCombo.setDisable(false);
            }
        });
        semesterCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && courseCombo.getValue() != null && yearCombo.getValue() != null) {
                handleSearch(null);
            }
        });

        // Setup table columns
        courseColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(cellData.getValue().getCourse().getClassCode().getcourseCode());
            } catch (Exception e) {
                System.out.println("Error getting course code: " + e.getMessage());
                return new SimpleStringProperty("Error");
            }
        });
        semesterColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(cellData.getValue().getCourse().getSemester().getName().name());
            } catch (Exception e) {
                System.out.println("Error getting semester: " + e.getMessage());
                return new SimpleStringProperty("Error");
            }
        });
        yearColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(cellData.getValue().getCourse().getSchoolYear().getName());
            } catch (Exception e) {
                System.out.println("Error getting year: " + e.getMessage());
                return new SimpleStringProperty("Error");
            }
        });

        // Initialize edit combo boxes with the same data
        editCourseCombo.setItems(courseCombo.getItems());
        editSemesterCombo.setItems(semesterCombo.getItems());
        editYearCombo.setItems(yearCombo.getItems());

        // Initialize response table columns
        questionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQuestion()));
        responseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResponse()));
        responseTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
    }

    private void updateYearOptionsForCourse(String courseWithName) {
        String courseCode = courseWithName.split(" - ")[0];
        List<String> years = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
            .filter(eval -> eval.getEvalType() == CourseEval.EvalType.INSTRUCTOR) // Only get course evaluations
            .filter(eval -> eval.getCourse() != null && eval.getCourse().getClassCode() != null && 
                    eval.getCourse().getClassCode().getcourseCode().equals(courseCode))
            .map(eval -> eval.getCourse().getSchoolYear().getName())
            .distinct()
            .collect(Collectors.toList());
        yearCombo.setItems(FXCollections.observableArrayList(years));
    }

    private void updateSemesterOptionsForCourseYear(String courseWithName, String year) {
        String courseCode = courseWithName.split(" - ")[0];
        List<String> semesters = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
            .filter(eval -> eval.getEvalType() == CourseEval.EvalType.INSTRUCTOR) // Only get course evaluations
            .filter(eval -> eval.getCourse() != null && eval.getCourse().getClassCode() != null && 
                    eval.getCourse().getClassCode().getcourseCode().equals(courseCode))
            .filter(eval -> eval.getCourse().getSchoolYear() != null && 
                    eval.getCourse().getSchoolYear().getName().equals(year))
            .map(eval -> eval.getCourse().getSemester().getName().name())
            .distinct()
            .collect(Collectors.toList());
        semesterCombo.setItems(FXCollections.observableArrayList(semesters));
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        String courseWithName = courseCombo.getValue();
        String semester = semesterCombo.getValue();
        String year = yearCombo.getValue();

        if (courseWithName == null || semester == null || year == null) {
            showAlert("Please select all search criteria");
            return;
        }

        try {
            // Extract course code from the combined string (e.g., "PA 400 - Evidence-Based...")
            String courseCode = courseWithName.split(" - ")[0];
            
            // Get all evaluations first
            List<CourseEval> allEvaluations = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

            // Debug: Print search criteria
            System.out.println("\nSearch criteria:");
            System.out.println("  Course code: " + courseCode);
            System.out.println("  Semester: " + semester);
            System.out.println("  Year: " + year);
            
            // Filter evaluations - only get INSTRUCTOR type evaluations (course evaluations)
            List<CourseEval> evaluations = allEvaluations.stream()
                .filter(eval -> eval.getEvalType() == CourseEval.EvalType.INSTRUCTOR)
                .filter(eval -> {
                    try {
                        boolean courseMatches = eval.getCourse().getClassCode().getcourseCode().equals(courseCode);
                        boolean semesterMatches = eval.getCourse().getSemester().getName().name().equals(semester);
                        boolean yearMatches = eval.getCourse().getSchoolYear().getName().equals(year);
                        
                        return courseMatches && semesterMatches && yearMatches;
                    } catch (Exception e) {
                        System.out.println("Error checking evaluation match: " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());

            // Debug: Print number of matches
            System.out.println("\nNumber of matches found: " + evaluations.size());

            if (evaluations.isEmpty()) {
                showAlert("No evaluations found matching the selected criteria");
            }

            evalTable.setItems(FXCollections.observableArrayList(evaluations));
        } catch (Exception e) {
            System.out.println("Error during search: " + e.getMessage());
            e.printStackTrace();
            showAlert("An error occurred while searching: " + e.getMessage());
        }
    }

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
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About SFU PA");
        alert.setHeaderText("SFU PA Application");
        alert.setContentText("This application is designed to manage gradebooks, instructor evaluations, and guest lecturers.");
        alert.showAndWait();
    }

    @FXML
    public void handleEvalSelection(MouseEvent event) {
        selectedEvaluation = evalTable.getSelectionModel().getSelectedItem();
        if (selectedEvaluation != null) {
            // Populate edit fields
            Classes classes = selectedEvaluation.getCourse();
            if (classes != null) {
                Course course = classes.getClassCode();
                if (course != null) {
                    editCourseCombo.setValue(course.getcourseCode() + " - " + course.getName());
                }
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
    public void handleSaveChanges(ActionEvent event) {
        if (selectedEvaluation == null) {
            showAlert("Please select an evaluation to edit");
            return;
        }

        try {
            // Update course details
            String selectedCourseWithName = editCourseCombo.getValue();
            String selectedSemester = editSemesterCombo.getValue();
            String selectedYear = editYearCombo.getValue();
            
            if (selectedCourseWithName == null || selectedSemester == null || selectedYear == null) {
                showAlert("Please select all fields before saving changes");
                return;
            }
            
            String selectedCourseCode = selectedCourseWithName.split(" - ")[0];
            
            // Find the course
            Course course = StreamSupport.stream(courseRepository.findAll().spliterator(), false)
                .filter(c -> c.getcourseCode().equals(selectedCourseCode))
                .findFirst()
                .orElse(null);
                
            if (course == null) {
                showAlert("Selected course not found");
                return;
            }
            
            // Find the semester
            SemesterName semesterEnum = SemesterName.valueOf(selectedSemester);
            Semester semester = semesterRepository.findById(semesterEnum.getId()).orElse(null);
            
            if (semester == null) {
                showAlert("Selected semester not found");
                return;
            }
            
            // Find the school year
            SchoolYear schoolYear = schoolYearRepository.findByName(selectedYear);
            
            if (schoolYear == null) {
                showAlert("Selected year not found");
                return;
            }
            
            // Update the course information
            Classes classes = selectedEvaluation.getCourse();
            classes.setClassCode(course);
            classes.setSemester(semester);
            classes.setSchoolYear(schoolYear);
            
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Course Evaluation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
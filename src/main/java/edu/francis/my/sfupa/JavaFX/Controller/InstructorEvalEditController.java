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
import java.util.Arrays;
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
    private ComboBox<String> instructorCombo;

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
        // Debug: Print repository status
        System.out.println("Initializing InstructorEvalEditController...");
        
        // Make table editable
        responsesTable.setEditable(true);
        
        // Debug: Check if repositories are autowired
        System.out.println("CourseEvalRepository: " + (courseEvalRepository != null ? "injected" : "null"));
        System.out.println("CourseRepository: " + (courseRepository != null ? "injected" : "null"));
        System.out.println("SchoolYearRepository: " + (schoolYearRepository != null ? "injected" : "null"));

        // Initialize combo boxes
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

        System.out.println("Available courses: " + courseOptions);
        courseCombo.setItems(FXCollections.observableArrayList(courseOptions));

        // Update semester values to match database format (UPPERCASE)
        List<String> semesterNames = Arrays.stream(SemesterName.values())
            .map(Enum::name)
            .collect(Collectors.toList());
        semesterCombo.setItems(FXCollections.observableArrayList(semesterNames));

        List<String> years = StreamSupport.stream(schoolYearRepository.findAll().spliterator(), false)
            .map(SchoolYear::getName)
            .collect(Collectors.toList());
        System.out.println("Available years: " + years);
        yearCombo.setItems(FXCollections.observableArrayList(years));

        // Setup table columns
        courseColumn.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(cellData.getValue().getCourse().getClassCode().getcourseCode());
            } catch (Exception e) {
                System.out.println("Error getting course code: " + e.getMessage());
                return new SimpleStringProperty("Error");
            }
        });
        
        instructorColumn.setCellValueFactory(cellData -> {
            try {
                Lecturer lecturer = cellData.getValue().getLecturer();
                return new SimpleStringProperty(lecturer != null ? 
                    lecturer.getFName() + " " + lecturer.getLName() : "N/A");
            } catch (Exception e) {
                System.out.println("Error getting lecturer name: " + e.getMessage());
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

        // Initialize instructor combo box
        List<String> instructors = StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
            .map(lecturer -> lecturer.getFName() + " " + lecturer.getLName())
            .collect(Collectors.toList());
        instructorCombo.setItems(FXCollections.observableArrayList(instructors));

        // Initialize response table columns
        questionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getQuestion()));
        responseColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResponse()));
        responseTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
    }

    @FXML
    public void handleSearch(ActionEvent event) {
        String courseCode = courseCombo.getValue();
        String semester = semesterCombo.getValue();
        String year = yearCombo.getValue();

        if (courseCode == null || semester == null || year == null) {
            showAlert("Please select all search criteria");
            return;
        }

        try {
            // Get all evaluations first
            List<CourseEval> allEvaluations = StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

            // Debug: Print total number of evaluations and their details
            System.out.println("\nTotal evaluations in database: " + allEvaluations.size());
            System.out.println("\nAll evaluations:");
            allEvaluations.forEach(eval -> {
                try {
                    System.out.println("Evaluation ID: " + eval.getId());
                    System.out.println("  Course: " + eval.getCourse().getClassCode().getcourseCode());
                    System.out.println("  Semester: " + eval.getCourse().getSemester().getName().name());
                    System.out.println("  Year: " + eval.getCourse().getSchoolYear().getName());
                    System.out.println("  Lecturer: " + (eval.getLecturer() != null ? 
                        eval.getLecturer().getFName() + " " + eval.getLecturer().getLName() : "N/A"));
                } catch (Exception e) {
                    System.out.println("Error printing evaluation details: " + e.getMessage());
                }
            });

            // Filter evaluations
            List<CourseEval> evaluations = allEvaluations.stream()
                .filter(eval -> {
                    try {
                        boolean courseMatches = eval.getCourse().getClassCode().getcourseCode().equals(courseCode);
                        boolean semesterMatches = eval.getCourse().getSemester().getName().name().equals(semester);
                        boolean yearMatches = eval.getCourse().getSchoolYear().getName().equals(year);
                        
                        System.out.println("\nChecking evaluation " + eval.getId() + ":");
                        System.out.println("  Course matches: " + courseMatches + 
                            " (Expected: " + courseCode + ", Got: " + eval.getCourse().getClassCode().getcourseCode() + ")");
                        System.out.println("  Semester matches: " + semesterMatches + 
                            " (Expected: " + semester + ", Got: " + eval.getCourse().getSemester().getName().name() + ")");
                        System.out.println("  Year matches: " + yearMatches + 
                            " (Expected: " + year + ", Got: " + eval.getCourse().getSchoolYear().getName() + ")");
                        
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
            Lecturer lecturer = selectedEvaluation.getLecturer();
            if (lecturer != null) {
                instructorCombo.setValue(lecturer.getFName() + " " + lecturer.getLName());
            }
            
            Classes classes = selectedEvaluation.getCourse();
            if (classes != null) {
                editCourseCombo.setValue(classes.getClassCode().getcourseCode());
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
            // Update instructor
            String[] instructorName = instructorCombo.getValue().split(" ");
            List<Lecturer> lecturers = StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
                .filter(l -> l.getFName().equals(instructorName[0]) && l.getLName().equals(instructorName[1]))
                .collect(Collectors.toList());
            
            if (!lecturers.isEmpty()) {
                selectedEvaluation.setLecturer(lecturers.get(0));
            }

            // Update course details
            Classes classes = selectedEvaluation.getCourse();
            Course course = StreamSupport.stream(courseRepository.findAll().spliterator(), false)
                .filter(c -> c.getcourseCode().equals(editCourseCombo.getValue()))
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

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Instructor Evaluation");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
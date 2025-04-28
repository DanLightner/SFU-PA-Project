package edu.francis.my.sfupa.SQLite.Services;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CSVInstructorEval {

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private ResponseLikertRepository responseLikertRepository;

    @Autowired
    private ResponseOpenRepository responseOpenRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    // Hardcoded file path for testing - maintain existing path logic
    //private static final String DEFAULT_FILE_PATH = System.getProperty("user.home") + "/Downloads/Example of Instructor Eval.csv";

    /**
     * Creates a new CourseEval entry with manually entered data
     * @param courseCode Course code (must exist in the database)
     * @param semesterId Semester ID
     * @param schoolYearId School year ID
     * @return The created CourseEval object or null if failed
     */
    public CourseEval createManualCourseEval(String courseCode, Long semesterId, Long schoolYearId) {
        try {
            // Find course
            Optional<Course> courseOpt = courseRepository.findById(courseCode);
            if (courseOpt.isEmpty()) {
                System.out.println("Course not found: " + courseCode);
                return null;
            }

            // Find or create class
            Classes classEntity = findOrCreateClass(courseOpt.get(), semesterId, schoolYearId);
            if (classEntity == null) {
                return null;
            }

            // Create course evaluation
            CourseEval courseEval = new CourseEval();
            courseEval.setCourse(classEntity);
            courseEval.setEvalType(CourseEval.EvalType.INSTRUCTOR);
            // Lecturer is optional, so we don't set it here

            return courseEvalRepository.save(courseEval);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates a new CourseEval entry with manually entered data including lecturer
     * @param courseCode Course code (must exist in the database)
     * @param semesterId Semester ID
     * @param schoolYearId School year ID
     * @param lecturerId Lecturer ID (optional, can be null)
     * @return The created CourseEval object or null if failed
     */
    public CourseEval createManualCourseEval(String courseCode, Long semesterId, Long schoolYearId, Long lecturerId) {
        try {
            // Find course
            Optional<Course> courseOpt = courseRepository.findById(courseCode);
            if (courseOpt.isEmpty()) {
                System.out.println("Course not found: " + courseCode);
                return null;
            }

            // Find or create class
            Classes classEntity = findOrCreateClass(courseOpt.get(), semesterId, schoolYearId);
            if (classEntity == null) {
                return null;
            }

            // Create course evaluation
            CourseEval courseEval = new CourseEval();
            courseEval.setCourse(classEntity);
            courseEval.setEvalType(CourseEval.EvalType.GUEST_LECTURER);
            
            // Set lecturer if provided
            if (lecturerId != null) {
                Optional<Lecturer> lecturerOpt = lecturerRepository.findById(lecturerId.intValue());
                if (lecturerOpt.isPresent()) {
                    courseEval.setLecturer(lecturerOpt.get());
                }
            }

            return courseEvalRepository.save(courseEval);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Classes findOrCreateClass(Course course, Long semesterId, Long schoolYearId) {
        Iterable<Classes> classes = classesRepository.findAll();
        for (Classes classItem : classes) {
            if (classItem.getClassCode().getcourseCode().equals(course.getcourseCode()) &&
                    Objects.equals(classItem.getSemester().getId(), semesterId) &&
                    classItem.getSchoolYear().getIdSchoolYear().equals(schoolYearId)) {
                return classItem;
            }
        }
        return null;
    }

    /**
     * Opens a file chooser dialog and processes the selected CSV file
     * @param stage The JavaFX stage to display the file chooser
     * @param courseEvalId The ID of the course evaluation to attach questions and responses to
     * @return true if the file was successfully processed, false otherwise
     */

    /*
    public boolean uploadCSV(Stage stage, Integer courseEvalId) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setTitle("Select Instructor Evaluation CSV File");

            File file = fileChooser.showOpenDialog(stage);

            // Check if the user selected a file
            if (file == null) {
                System.out.println("No file selected.");
                return false;
            }

            processCSVFile(file, courseEvalId);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    */


    /**
     * Processes the CSV file and adds the data to the database
     * @param file The CSV file to process
     * @param courseEvalId The ID of the course evaluation to attach questions and responses to
     */
    public void processCSVFile(File file, Integer courseEvalId) throws IOException {
        Optional<CourseEval> courseEvalOpt = courseEvalRepository.findById(courseEvalId);

        if (courseEvalOpt.isEmpty()) {
            throw new IllegalArgumentException("Course evaluation not found");
        }

        CourseEval courseEval = courseEvalOpt.get();

        try (FileReader fileReader = new FileReader(file);
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            Map<Integer, Questions> questionMap = new HashMap<>();

            Pattern questionPattern = Pattern.compile("(\\d+):\\s+(.*)");

            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                String header = entry.getKey();
                Matcher matcher = questionPattern.matcher(header);

                if (matcher.find()) {
                    String questionId = matcher.group(1);
                    String questionText = matcher.group(2);

                    boolean isLikert = !questionText.toLowerCase().contains("name") &&
                            !questionText.toLowerCase().contains("suggest") &&
                            !questionText.toLowerCase().contains("comment");

                    Questions question = new Questions(questionText, isLikert);
                    question.setEval(courseEval);
                    question = questionsRepository.save(question);

                    questionMap.put(entry.getValue(), question);
                }
            }

            List<CSVRecord> records = csvParser.getRecords();
            for (CSVRecord record : records) {
                for (Map.Entry<Integer, Questions> entry : questionMap.entrySet()) {
                    int columnIndex = entry.getKey();
                    Questions question = entry.getValue();
                    String response = record.get(columnIndex);

                    if (response == null || response.trim().isEmpty()) {
                        continue;
                    }

                    if (question.getType()) {
                        ResponseLikert likertResponse = new ResponseLikert(response, courseEval, question);
                        responseLikertRepository.save(likertResponse);
                    } else {
                        ResponseOpen openResponse = new ResponseOpen(response, courseEval, question);
                        responseOpenRepository.save(openResponse);
                    }
                }
            }
        }
    }

    /**
     * Processes a CSV file with the specified path
     * @param filePath The path to the CSV file
     * @param courseEvalId The ID of the course evaluation to attach questions and responses to
     * @return true if the file was successfully processed, false otherwise
     */
    /*
    public boolean processCSVByPath(String filePath, Integer courseEvalId) {
        System.out.println("Resolved file path: " + filePath);
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("File does not exist: " + filePath);
                return false;
            }

            processCSVFile(file, courseEvalId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    */

    /**
     * For testing purposes - allows running the CSV import with hardcoded values
     * This method can be called from any controller or test code
     */

    /*
    public void runHardcodedImport() {
        try {
            // First create a course evaluation with hardcoded values
            // These would normally be manually entered by the user
            String courseCode = "CS101";
            Long semesterId = 1L;
            Long schoolYearId = 1L;
            String lecturerFirstName = "John";
            String lecturerLastName = "Smith";


            //WILL NEED TO CHANGE THIS LATER!!! ABSOLUTELY!!
            CourseEval courseEval = createManualCourseEval(courseCode, semesterId, schoolYearId,
                    lecturerFirstName, lecturerLastName);

            if (courseEval == null) {
                System.out.println("Failed to create course evaluation for import test");
                return;
            }

            // Now import the CSV data for this course evaluation
            String testFilePath = DEFAULT_FILE_PATH;
            System.out.println("Starting hardcoded import with file: " + testFilePath);

            boolean success = processCSVByPath(testFilePath, courseEval.getId());

            if (success) {
                System.out.println("Hardcoded import completed successfully");
            } else {
                System.out.println("Hardcoded import failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error during hardcoded import");
        }


    }


     */
}
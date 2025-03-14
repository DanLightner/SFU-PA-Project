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
    private LecturerRepository lecturerRepository;

    // Hardcoded file path for testing - modify this as needed
    private static final String DEFAULT_FILE_PATH = "C:\\Users\\danli\\Downloads\\Example of Instructor Eval.csv";

    /**
     * Opens a file chooser dialog and processes the selected CSV file
     * @param stage The JavaFX stage to display the file chooser
     * @param classId The ID of the class for this evaluation
     * @param lecturerId The ID of the lecturer being evaluated
     * @return true if the file was successfully processed, false otherwise
     */
    public boolean uploadCSV(Stage stage, Long classId, Long lecturerId) {
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

            processCSVFile(file, classId, lecturerId);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Processes the CSV file and adds the data to the database
     * @param file The CSV file to process
     * @param classId The ID of the class for this evaluation
     * @param lecturerId The ID of the lecturer being evaluated
     */
    public void processCSVFile(File file, Long classId, Long lecturerId) throws IOException {
        // Get class and lecturer entities
        Optional<Classes> classOpt = classesRepository.findById(classId.intValue());
        Optional<Lecturer> lecturerOpt = lecturerRepository.findById(lecturerId.intValue());

        if (classOpt.isEmpty() || lecturerOpt.isEmpty()) {
            throw new IllegalArgumentException("Class or lecturer not found");
        }

        Classes classEntity = classOpt.get();
        Lecturer lecturer = lecturerOpt.get();

        // Create a new course evaluation
        CourseEval courseEval = new CourseEval();
        courseEval.setCourse(classEntity);
        courseEval.setLecturer(lecturer);
        courseEval = courseEvalRepository.save(courseEval);

        // Read CSV using Apache Commons CSV
        try (FileReader fileReader = new FileReader(file);
             CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            // Get headers and process them to identify question columns
            Map<String, Integer> headerMap = csvParser.getHeaderMap();
            Map<Integer, Questions> questionMap = new HashMap<>();

            // Pattern to match question IDs in column headers
            Pattern questionPattern = Pattern.compile("(\\d+):\\s+(.*)");

            // Process headers to identify question columns and create question entities
            for (Map.Entry<String, Integer> entry : headerMap.entrySet()) {
                String header = entry.getKey();
                Matcher matcher = questionPattern.matcher(header);

                if (matcher.find()) {
                    String questionId = matcher.group(1);
                    String questionText = matcher.group(2);

                    // Determine if it's a Likert scale or open-ended question
                    boolean isLikert = !questionText.toLowerCase().contains("name") &&
                            !questionText.toLowerCase().contains("suggest") &&
                            !questionText.toLowerCase().contains("comment");

                    Questions question = new Questions(questionText, isLikert);
                    question.setEval(courseEval);
                    question = questionsRepository.save(question);

                    questionMap.put(entry.getValue(), question);
                }
            }

            // Process records (each record is a student's evaluation)
            List<CSVRecord> records = csvParser.getRecords();
            for (CSVRecord record : records) {
                // Process each question column for this record
                for (Map.Entry<Integer, Questions> entry : questionMap.entrySet()) {
                    int columnIndex = entry.getKey();
                    Questions question = entry.getValue();
                    String response = record.get(columnIndex);

                    // Skip empty responses
                    if (response == null || response.trim().isEmpty()) {
                        continue;
                    }

                    // Save the response based on question type
                    if (question.getType()) {
                        // Likert scale response
                        ResponseLikert likertResponse = new ResponseLikert(response, courseEval, question);
                        responseLikertRepository.save(likertResponse);
                    } else {
                        // Open-ended response
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
     * @param classId The ID of the class for this evaluation
     * @param lecturerId The ID of the lecturer being evaluated
     * @return true if the file was successfully processed, false otherwise
     */
    public boolean processCSVByPath(String filePath, Long classId, Long lecturerId) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("File does not exist: " + filePath);
                return false;
            }

            processCSVFile(file, classId, lecturerId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Convenience method to process a CSV file using the default hardcoded path
     * @param classId The ID of the class for this evaluation
     * @param lecturerId The ID of the lecturer being evaluated
     * @return true if the file was successfully processed, false otherwise
     */
    public boolean processDefaultCSV(Long classId, Long lecturerId) {
        return processCSVByPath(DEFAULT_FILE_PATH, classId, lecturerId);
    }

    /**
     * For testing purposes - allows running the CSV import with hardcoded values
     * This method can be called from any controller or test code
     */
    public void runHardcodedImport() {
        // Example hardcoded values - modify these as needed for your testing
        Long classId = 1L;  // Example class ID
        Long lecturerId = 1L;  // Example lecturer ID
        String testFilePath = "src/main/resources/test_data/instructor_eval_sample.csv";

        System.out.println("Starting hardcoded import with file: " + testFilePath);
        boolean success = processCSVByPath(testFilePath, classId, lecturerId);

        if (success) {
            System.out.println("Hardcoded import completed successfully");
        } else {
            System.out.println("Hardcoded import failed");
        }
    }
}
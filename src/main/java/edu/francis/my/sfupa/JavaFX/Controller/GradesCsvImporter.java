package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.Classes;
import edu.francis.my.sfupa.SQLite.Models.Student;
import edu.francis.my.sfupa.SQLite.Models.Grade;
import edu.francis.my.sfupa.SQLite.Repository.ClassesRepository;
import edu.francis.my.sfupa.SQLite.Repository.StudentRepository;
import edu.francis.my.sfupa.SQLite.Repository.GradeRepository;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.opencsv.CSVReader;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GradesCsvImporter {

    private static final Logger log = LoggerFactory.getLogger(GradesCsvImporter.class);
    private static final int EXPECTED_FIELDS = 5;

    private GradeRepository gradeRepository;
    private ClassesRepository classesRepository;
    private StudentRepository studentRepository;

    // Constructor to initialize repositories
    public GradesCsvImporter(GradeRepository gradeRepository,
                             ClassesRepository classesRepository,
                             StudentRepository studentRepository) {
        this.gradeRepository = gradeRepository;
        this.classesRepository = classesRepository;
        this.studentRepository = studentRepository;
    }

    // Method to prompt the user for a CSV file and import it
    public void importCsv(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Grades CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            processCsvFile(selectedFile);
        } else {
            log.warn("No file selected.");
        }
    }

    // Method to process the selected CSV file and import data into the database
    private void processCsvFile(File file) {
        List<Grade> gradeList = new ArrayList<>();

        try (CSVReader reader = new CSVReaderBuilder(new FileReader(file))
                .withSkipLines(1)
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(',')
                        .withIgnoreLeadingWhiteSpace(true)
                        .withIgnoreQuotations(false)
                        .build())
                .build()) {

            String[] line;
            int rowNumber = 1;

            while ((line = reader.readNext()) != null) {
                rowNumber++;

                // Skip empty rows
                if (line.length == 0 || (line.length == 1 && line[0].trim().isEmpty())) {
                    continue;
                }

                // Always ensure we have exactly EXPECTED_FIELDS elements
                if (line.length < EXPECTED_FIELDS) {
                    line = Arrays.copyOf(line, EXPECTED_FIELDS);
                }

                // Trim all values and replace nulls
                for (int i = 0; i < line.length; i++) {
                    if (line[i] != null) {
                        line[i] = line[i].trim();
                    } else {
                        line[i] = "";
                    }
                }

                // Parse using defaults if necessary
                boolean retake = NumberUtils.toInt(line[0], 0) == 1;
                int classId = NumberUtils.toInt(line[1], -1);
                int gradeId = NumberUtils.toInt(line[2], -1);
                long studentId = NumberUtils.toLong(line[3], 0L);
                String gradeValue = line[4].isEmpty() ? "N/A" : line[4];

                // Log problematic data but continue
                if (classId < 0) {
                    log.warn("Row {}: invalid classId '{}', defaulting to -1", rowNumber, line[1]);
                }
                if (studentId == 0L) {
                    log.warn("Row {}: invalid or missing studentId '{}', defaulting to 0", rowNumber, line[3]);
                }

                // Lookup or create student and class
                int finalRowNumber = rowNumber;
                Student student = studentRepository.findById(studentId).orElseGet(() -> {
                    Student s = new Student();
                    s.setId_student(studentId);
                    try {
                        return studentRepository.save(s);
                    } catch (Exception e) {
                        log.error("Failed to create student at row {}: {}", finalRowNumber, e.getMessage());
                        return null;
                    }
                });

                Classes course = classesRepository.findById(classId).orElse(null);
                if (course == null) {
                    log.warn("Row {}: course not found with ID {}", rowNumber, classId);
                    continue;
                }
                if (student == null) {
                    continue; // skip if student creation failed
                }

                // Build grade entity
                Grade gradeEntity = new Grade();
                gradeEntity.setRetake(retake);
                gradeEntity.setStudent(student);
                gradeEntity.setStudentClass(course);
                gradeEntity.setGrade(gradeValue);

                gradeList.add(gradeEntity);
            }

            // Bulk save
            if (!gradeList.isEmpty()) {
                gradeRepository.saveAll(gradeList);
                log.info("Successfully imported {} grades!", gradeList.size());
            } else {
                log.info("No valid data found to import.");
            }

        } catch (Exception e) {
            log.error("Error processing CSV file: {}", e.getMessage(), e);
        }
    }
}

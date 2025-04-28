package edu.francis.my.sfupa.JavaFX.Controller;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import edu.francis.my.sfupa.SQLite.Models.Classes;
import edu.francis.my.sfupa.SQLite.Models.Grade;
import edu.francis.my.sfupa.SQLite.Models.Student;
import edu.francis.my.sfupa.SQLite.Repository.ClassesRepository;
import edu.francis.my.sfupa.SQLite.Repository.GradeRepository;
import edu.francis.my.sfupa.SQLite.Repository.StudentRepository;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GradesCsvImporter {
    private static final Logger log = LoggerFactory.getLogger(GradesCsvImporter.class);

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final ClassesRepository classesRepository;

    public GradesCsvImporter(GradeRepository gradeRepository,
                             StudentRepository studentRepository,
                             ClassesRepository classesRepository) {
        this.gradeRepository = gradeRepository;
        this.studentRepository = studentRepository;
        this.classesRepository = classesRepository;
    }

    public void importFromCsv(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        var file = chooser.showOpenDialog(stage);
        if (file == null) return;

        try (var reader = new CSVReaderBuilder(new FileReader(file))
                .withCSVParser(new CSVParserBuilder().withSeparator(',').build())
                .build()) {

            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) {
                log.info("CSV is empty");
                return;
            }

            // Identify columns
            String[] header = rows.get(0);
            int idxId    = Arrays.asList(header).indexOf("StudentID");
            int idxScore = Arrays.asList(header).indexOf("Score");
            int idxCourse = Arrays.asList(header).indexOf("CourseID");
            int idxRetake = Arrays.asList(header).indexOf("Retake");  // may be -1

            List<Grade> gradeList = new ArrayList<>();

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                if (!NumberUtils.isParsable(row[idxId]) || !NumberUtils.isParsable(row[idxScore])) {
                    log.warn("Skipping invalid row {}: {}", i + 1, Arrays.toString(row));
                    continue;
                }

                Student student = studentRepository.findById(Long.valueOf(Integer.valueOf(row[idxId]))).orElse(null);
                Classes course  = classesRepository.findById(Integer.valueOf(row[idxCourse])).orElse(null);
                if (student == null || course == null) {
                    log.warn("Unknown student or course in row {}: {}", i + 1, Arrays.toString(row));
                    continue;
                }

                Grade grade = new Grade();
                grade.setStudent(student);
                grade.setStudentClass(course);
                grade.setGrade(String.valueOf(Double.valueOf(row[idxScore])));

                // NEW: Retake flag
                if (idxRetake >= 0 && row.length > idxRetake) {
                    grade.setRetake(Boolean.parseBoolean(row[idxRetake].trim()));
                }

                gradeList.add(grade);
            }

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

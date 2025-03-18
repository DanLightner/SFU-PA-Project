package edu.francis.my.sfupa.SQLite.Services;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.CourseRepository;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

public class CSVGrade {

    public void uploadCSV(Stage stage) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

            File file = fileChooser.showOpenDialog(stage);

            // Check if the user selected a file
            if (file == null) {
                System.out.println("No file selected.");
                return;
            }

            // Read CSV using Apache Commons CSV
            FileReader fileReader = new FileReader(file);
            CSVParser csvParser = new CSVParser(fileReader, CSVFormat.DEFAULT.withFirstRecordAsHeader());

            List<CSVRecord> csvRecords = csvParser.getRecords();

            for (CSVRecord record : csvRecords) {
                Grade gradeRecord = new Grade(new Student(Long.parseLong(record.get("Student ID "))), new Classes(new Course("CPSC410","Some class"),new Semester(SemesterName.Fall), new SchoolYear("2024-2025")), record.get("Final Grade "), false);
                //Student student = students.get(record.get("Student ID "));
                String grade = record.get("Final Grade ");
            }

            // Close resources
            csvParser.close();
            fileReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

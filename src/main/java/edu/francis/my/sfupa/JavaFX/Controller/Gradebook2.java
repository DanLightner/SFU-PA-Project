package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.ClassesRepository;
import edu.francis.my.sfupa.SQLite.Repository.CourseRepository;
import edu.francis.my.sfupa.SQLite.Repository.LecturerRepository;
import edu.francis.my.sfupa.SQLite.Repository.StudentRepository;
import edu.francis.my.sfupa.SQLite.Services.CSVGrade2;
import edu.francis.my.sfupa.SQLite.Services.CourseEvalService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class Gradebook2 {

    @Autowired
    private CSVGrade2 CSVGrade2;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private StudentRepository studentRepository;

    @FXML
    private ComboBox semesterCombo;
    @Autowired
    private CourseRepository courseRepository;

    public void importCsv(MultipartFile file) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            List<CourseEval> courseEvalList = new ArrayList<>();
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }

                String[] values = line.split(",");
                if (values.length < 3) continue; // Skip invalid lines

                Integer studentId = Integer.parseInt(values[1]);
                

                Student student=studentRepository.findById(studentId).get();
                //Course course = courseRepository.findById(courseId).orElse(null);
                String semester=semesterCombo.getValue().toString();


            }




        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
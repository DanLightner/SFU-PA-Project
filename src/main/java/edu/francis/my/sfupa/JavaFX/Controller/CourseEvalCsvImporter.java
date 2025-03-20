package edu.francis.my.sfupa.JavaFX.Controller;

import edu.francis.my.sfupa.SQLite.Models.CourseEval;
import edu.francis.my.sfupa.SQLite.Models.Classes;
import edu.francis.my.sfupa.SQLite.Models.Lecturer;
import edu.francis.my.sfupa.SQLite.Services.CourseEvalService;
import edu.francis.my.sfupa.SQLite.Repository.ClassesRepository;
import edu.francis.my.sfupa.SQLite.Repository.LecturerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class CourseEvalCsvImporter {

    @Autowired
    private CourseEvalService courseEvalService;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

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

                Integer courseId = Integer.parseInt(values[1]);
                Integer lecturerId = Integer.parseInt(values[2]);

                Classes course = classesRepository.findById(courseId).orElse(null);
                Lecturer lecturer = lecturerRepository.findById(lecturerId).orElse(null);

                if (course != null && lecturer != null) {
                    CourseEval courseEval = new CourseEval();
                    courseEval.setCourse(course);
                    courseEval.setLecturer(lecturer);
                    courseEvalList.add(courseEval);
                }
            }

            for (CourseEval eval : courseEvalList) {
                courseEvalService.save(eval);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
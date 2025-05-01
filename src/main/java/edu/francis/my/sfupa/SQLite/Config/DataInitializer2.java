package edu.francis.my.sfupa.SQLite.Config;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/*
@Configuration
public class DataInitializer2 {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private ResponseLikertRepository responseLikertRepository;

    @Autowired
    private ResponseOpenRepository responseOpenRepository;

    @Autowired
    private GradeRepository gradeRepository;

    private final Random random = new Random();

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // Only initialize if the database is empty
            if (courseRepository.count() == 0) {
                System.out.println("Initializing database with basic structure...");

                // Create courses
                List<Course> courses = initCourses();

                // Create semesters
                List<Semester> semesters = initSemesters();

                // Create school years
                List<SchoolYear> schoolYears = initSchoolYears();

                System.out.println("Database basic structure initialization complete!");
            } else {
                System.out.println("Database already contains data. Skipping initialization.");
            }
        };
    }

    private List<Course> initCourses() {
        List<Course> courses = Arrays.asList(
                new Course("PA 400", "Evidence-Based Medicine"),
                new Course("PA 401", "Introduction to U.S. Health Care"),
                new Course("PA 402", "History Taking and Patient Education Skills"),
                new Course("PA 403", "History Taking and Patient Education Skills Lab"),
                new Course("PA 404", "Public Health"),
                new Course("PA 405", "Clinical Skills"),
                new Course("PA 406", "Well Child"),
                new Course("PA 420", "Introduction to Medicine Module"),
                new Course("PA 421", "Hematology Medicine Module"),
                new Course("PA 422", "Endocrine Medicine Module"),
                new Course("PA 423", "Neurology Medicine Module"),
                new Course("PA 424", "Dermatology Medicine Module"),
                new Course("PA 425", "Musculoskeletal Medicine Module"),
                new Course("PA 426", "Eyes, Ears, Nose and Throat Medicine Module"),
                new Course("PA 427", "Behavioral Medicine Module"),
                new Course("PA 428", "Cardiovascular Medicine Module"),
                new Course("PA 429", "Pulmonary Medicine Module"),
                new Course("PA 430", "Gastrointestinal/Nutrition Medicine Module"),
                new Course("PA 431", "Genitourinary Medicine Module"),
                new Course("PA 432", "Reproductive Medicine Module"),
                new Course("PA 451", "Didactic Clinical Experiences and Medical Documentation I"),
                new Course("PA 452", "Didactic Clinical Experiences and Medical Documentation II"),
                new Course("PA 453", "Didactic Comprehensive Evaluation")
        );

        courseRepository.saveAll(courses);
        System.out.println("Created " + courses.size() + " courses");
        return courses;
    }

    private List<Semester> initSemesters() {
        List<Semester> semesters = new ArrayList<>();
        for (SemesterName name : SemesterName.values()) {
            Semester semester = new Semester();
            semester.setName(name);
            semesters.add(semester);
        }

        semesterRepository.saveAll(semesters);
        System.out.println("Created " + semesters.size() + " semesters");
        return (List<Semester>) semesterRepository.findAll();
    }

    private List<SchoolYear> initSchoolYears() {
        List<SchoolYear> schoolYears = Arrays.asList(
                // new SchoolYear("2020-2021"),
                // new SchoolYear("2021-2022"),
                // new SchoolYear("2022-2023"),
                new SchoolYear("2023-2024"),
                new SchoolYear("2024-2025")
                // new SchoolYear("2025-2026"),
                // new SchoolYear("2026-2027"),
                // new SchoolYear("2027-2028"),
                // new SchoolYear("2028-2029"),
                // new SchoolYear("2029-2030"),
                // new SchoolYear("2030-2031"),
                // new SchoolYear("2031-2032"),
                // new SchoolYear("2032-2033"),
                // new SchoolYear("2033-2034"),
                // new SchoolYear("2034-2035")
        );

        schoolYearRepository.saveAll(schoolYears);
        System.out.println("Created " + schoolYears.size() + " school years");
        return (List<SchoolYear>) schoolYearRepository.findAll();
    }

    // Keep these methods for reference but don't use them in initialization
    private List<Questions> getDefaultQuestions() {
        // Define some standard questions that can be used when creating new evaluations
        String[] likertQuestions = {
                "The instructor presented material clearly",
                "The course was well organized",
                "The instructor was responsive to student questions",
                "The assignments were relevant to the course material",
                "The exams fairly represented the course material",
                "The instructor was available during office hours",
                "The instructor provided timely feedback",
                "The course workload was appropriate"
        };

        String[] openQuestions = {
                "What aspects of this course were most beneficial to you?",
                "How could this course be improved?",
                "What suggestions do you have for the instructor?",
                "Additional comments about the course"
        };

        List<Questions> defaultQuestions = new ArrayList<>();

        // Add Likert scale questions (type = true)
        for (String questionText : likertQuestions) {
            defaultQuestions.add(new Questions(questionText, true));
        }

        // Add open-ended questions (type = false)
        for (String questionText : openQuestions) {
            defaultQuestions.add(new Questions(questionText, false));
        }

        return defaultQuestions;
    }
}
*/
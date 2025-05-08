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

@Configuration
public class DataInitializer {

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


    /*

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
                System.out.println("Initializing database with test data...");

                // Create courses
                List<Course> courses = initCourses();

                // Create lecturers
                List<Lecturer> lecturers = initLecturers();

                // Create semesters
                List<Semester> semesters = initSemesters();

                // Create school years
                List<SchoolYear> schoolYears = initSchoolYears();

                // Create classes (combining courses, semesters, and school years)
                List<Classes> classes = initClasses(courses, semesters, schoolYears);

                // Create students
                List<Student> students = initStudents();

                // Create course evaluations
                List<CourseEval> courseEvals = initCourseEvals(classes, lecturers);

                // Create questions
                List<Questions> questions = initQuestions(courseEvals);

                // Create responses
                initResponses(courseEvals, questions);

                // Create grades
                initGrades(classes, students);

                System.out.println("Database initialization complete!");
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

    private List<Lecturer> initLecturers() {
        List<Lecturer> lecturers = Arrays.asList(
                new Lecturer("John", "Smith"),
                new Lecturer("Jane", "Doe"),
                new Lecturer("Robert", "Johnson"),
                new Lecturer("Mary", "Williams"),
                new Lecturer("Michael", "Brown"),
                new Lecturer("Patricia", "Jones"),
                new Lecturer("James", "Miller"),
                new Lecturer("Linda", "Davis"),
                new Lecturer("William", "Garcia"),
                new Lecturer("Elizabeth", "Rodriguez")
        );

        // Fix the initialization error by manually setting IDs
        lecturerRepository.saveAll(lecturers);
        System.out.println("Created " + lecturers.size() + " lecturers");
        return (List<Lecturer>) lecturerRepository.findAll();
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
              //  new SchoolYear("2021-2022"),
              //  new SchoolYear("2022-2023"),
                new SchoolYear("2023-2024"),
                new SchoolYear("2024-2025")
               // new SchoolYear("2025-2026"),
               // new SchoolYear("2026-2027"),
               // new SchoolYear("2027-2028"),
               // new SchoolYear("2028-2029"),
              //  new SchoolYear("2029-2030"),
                //new SchoolYear("2030-2031"),
              //  new SchoolYear("2031-2032"),
               // new SchoolYear("2032-2033"),
             //   new SchoolYear("2033-2034"),
               // new SchoolYear("2034-2035")
        );


        schoolYearRepository.saveAll(schoolYears);
        System.out.println("Created " + schoolYears.size() + " school years");
        return (List<SchoolYear>) schoolYearRepository.findAll();
    }

    private List<Classes> initClasses(List<Course> courses, List<Semester> semesters, List<SchoolYear> schoolYears) {
        List<Classes> classes = new ArrayList<>();

        for (Course course : courses) {
            // Create classes for each course in different semesters and school years
            for (Semester semester : semesters) {
                for (SchoolYear schoolYear : schoolYears) {
                    // Not every course is offered every semester/year
                    if (random.nextBoolean()) {
                        Classes classItem = new Classes(course, semester, schoolYear);
                        classes.add(classItem);
                    }
                }
            }
        }

        classesRepository.saveAll(classes);
        System.out.println("Created " + classes.size() + " classes");
        return (List<Classes>) classesRepository.findAll();
    }

    private List<Student> initStudents() {
        List<Student> students = new ArrayList<>();
        for (long i = 1000; i < 1100; i++) {
            Student student = new Student(i);
            students.add(student);
        }

        studentRepository.saveAll(students);
        System.out.println("Created " + students.size() + " students");
        return (List<Student>) studentRepository.findAll();
    }

    private List<CourseEval> initCourseEvals(List<Classes> classes, List<Lecturer> lecturers) {
        List<CourseEval> courseEvals = new ArrayList<>();

        for (Classes classItem : classes) {
            // Create exactly one evaluation per class
            // Select a random lecturer
            Lecturer lecturer = lecturers.get(random.nextInt(lecturers.size()));

            CourseEval courseEval = new CourseEval();
            courseEval.setCourse(classItem);
            courseEval.setLecturer(lecturer);
            
            // Randomly assign evaluation type (70% chance of INSTRUCTOR, 30% chance of GUEST_LECTURER)
            if (random.nextDouble() < 0.7) {
                courseEval.setEvalType(CourseEval.EvalType.INSTRUCTOR);
            } else {
                courseEval.setEvalType(CourseEval.EvalType.GUEST_LECTURER);
            }
            
            courseEvals.add(courseEval);
        }

        courseEvalRepository.saveAll(courseEvals);
        System.out.println("Created " + courseEvals.size() + " course evaluations");
        return (List<CourseEval>) courseEvalRepository.findAll();
    }

    private List<Questions> initQuestions(List<CourseEval> courseEvals) {
        // Define instructor-specific Likert questions
        String[] instructorLikertQuestions = {
                "The instructor presented material clearly",
                "The instructor was responsive to student questions",
                "The instructor was available during office hours",
                "The instructor provided timely feedback",
                "The instructor was well-prepared for class",
                "The instructor demonstrated knowledge of the subject",
                "The instructor effectively used class time",
                "The instructor created an environment conducive to learning"
        };

        // Define course-specific Likert questions
        String[] courseLikertQuestions = {
                "The course was well organized",
                "The assignments were relevant to the course material",
                "The exams fairly represented the course material",
                "The course workload was appropriate",
                "The course materials were helpful",
                "The course objectives were clear",
                "The course content was valuable",
                "The course pace was appropriate"
        };

        // Define instructor-specific open questions
        String[] instructorOpenQuestions = {
                "What did you find most effective about the instructor's teaching style?",
                "How could the instructor improve their teaching methods?",
                "What suggestions do you have for the instructor?",
                "Additional comments about the instructor"
        };

        // Define course-specific open questions
        String[] courseOpenQuestions = {
                "What aspects of this course were most beneficial to you?",
                "How could this course be improved?",
                "What aspects of the course content need more clarity?",
                "Additional comments about the course"
        };

        List<Questions> questions = new ArrayList<>();

        for (CourseEval eval : courseEvals) {
            if (eval.getEvalType() == CourseEval.EvalType.INSTRUCTOR) {
                // For instructor evaluations, only use course-related questions
                for (String questionText : courseLikertQuestions) {
                    Questions question = new Questions(questionText, true);
                    question.setEval(eval);
                    questions.add(question);
                }

                for (String questionText : courseOpenQuestions) {
                    Questions question = new Questions(questionText, false);
                    question.setEval(eval);
                    questions.add(question);
                }
            } else {
                // For guest lecturers, only use instructor-related questions
                for (String questionText : instructorLikertQuestions) {
                    Questions question = new Questions(questionText, true);
                    question.setEval(eval);
                    questions.add(question);
                }

                for (String questionText : instructorOpenQuestions) {
                    Questions question = new Questions(questionText, false);
                    question.setEval(eval);
                    questions.add(question);
                }
            }
        }

        questionsRepository.saveAll(questions);
        System.out.println("Created " + questions.size() + " questions");
        return (List<Questions>) questionsRepository.findAll();
    }

    private void initResponses(List<CourseEval> courseEvals, List<Questions> questions) {
        List<ResponseLikert> responsesLikert = new ArrayList<>();
        List<ResponseOpen> responsesOpen = new ArrayList<>();

        String[] likertOptions = {"Strongly Disagree", "Disagree", "Neutral", "Agree", "Strongly Agree"};
        String[] openResponses = {
                "The lectures were very informative and engaging.",
                "I appreciated the hands-on labs and practical exercises.",
                "Course material was relevant to real-world applications.",
                "More practice problems would be helpful.",
                "The textbook was difficult to follow at times.",
                "I enjoyed the group projects and collaborative work.",
                "The instructor's examples were very helpful in understanding concepts.",
                "Some topics were covered too quickly without enough explanation.",
                "I learned a lot from this course and would recommend it to others.",
                "The feedback on assignments was constructive and helped me improve."
        };

        // For each course evaluation and question, create responses
        for (Questions question : questions) {
            CourseEval eval = question.getEval();

            // Number of responses per question (1-10)
            int numResponses = random.nextInt(10) + 1;

            for (int i = 0; i < numResponses; i++) {
                if (question.getType()) {  // Likert question
                    ResponseLikert response = new ResponseLikert(
                            likertOptions[random.nextInt(likertOptions.length)],
                            eval,
                            question
                    );
                    responsesLikert.add(response);
                } else {  // Open question
                    ResponseOpen response = new ResponseOpen(
                            openResponses[random.nextInt(openResponses.length)],
                            eval,
                            question
                    );
                    responsesOpen.add(response);
                }
            }
        }

        responseLikertRepository.saveAll(responsesLikert);
        responseOpenRepository.saveAll(responsesOpen);
        System.out.println("Created " + responsesLikert.size() + " Likert responses and "
                + responsesOpen.size() + " open-ended responses");
    }

    private void initGrades(List<Classes> classes, List<Student> students) {
        List<Grade> grades = new ArrayList<>();
        String[] gradeValues = {"A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "F"};

        for (Classes classItem : classes) {
            // Assign 10-30 students to each class
            int numStudents = random.nextInt(21) + 10;

            for (int i = 0; i < numStudents; i++) {
                // Select a random student
                Student student = students.get(random.nextInt(students.size()));

                // Determine if it's a retake (10% chance)
                boolean retake = random.nextInt(100) < 10;

                // Assign a grade
                String grade = gradeValues[random.nextInt(gradeValues.length)];

                Grade gradeRecord = new Grade(student, classItem, grade, retake);
                grades.add(gradeRecord);
            }
        }

        gradeRepository.saveAll(grades);
        System.out.println("Created " + grades.size() + " grade records");
    }
    */
}
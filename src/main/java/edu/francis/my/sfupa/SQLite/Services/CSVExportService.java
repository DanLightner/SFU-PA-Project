package edu.francis.my.sfupa.SQLite.Services;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class CSVExportService {
    /*

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private ResponseLikertRepository responseLikertRepository;

    @Autowired
    private ResponseOpenRepository responseOpenRepository;

    @Autowired
    private QuestionsRepository questionsRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ClassesRepository classesRepository;

    private static final String CSV_DIRECTORY = "csv-exports";

    /**
     * Creates the export directory if it doesn't exist
     */

    /*
    public void createExportDirectoryIfNotExists() {
        Path path = Paths.get(CSV_DIRECTORY);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create export directory", e);
            }
        }
    }

    /**
     * Export all course evaluations with likert scale responses to CSV
     * @return Path to the created CSV file
     */

    /*
    public String exportLikertResponses() {
        createExportDirectoryIfNotExists();
        String fileName = CSV_DIRECTORY + "/likert_responses_" + System.currentTimeMillis() + ".csv";

        try (FileWriter fileWriter = new FileWriter(fileName);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT
                     .withHeader("Course Code", "Course Name", "Semester", "School Year",
                             "Lecturer", "Question", "Response", "Response Count"))) {

            // Fetch all responses
            Iterable<ResponseLikert> allResponses = responseLikertRepository.findAll();

            // Group by course, question, and response
            Map<String, Map<String, Map<String, Long>>> groupedData = StreamSupport
                    .stream(allResponses.spliterator(), false)
                    .collect(Collectors.groupingBy(
                            response -> {
                                CourseEval eval = response.getCourseEval();
                                Classes course = eval.getCourse();
                                return course.getClassCode().getcourseCode() + " - " +
                                        course.getSemester().getName() + " " +
                                        course.getSchoolYear().getName();
                            },
                            Collectors.groupingBy(
                                    response -> response.getQuestion().getText(),
                                    Collectors.groupingBy(
                                            ResponseLikert::getResponse,
                                            Collectors.counting()
                                    )
                            )
                    ));

            // Print data to CSV
            for (Map.Entry<String, Map<String, Map<String, Long>>> courseEntry : groupedData.entrySet()) {
                String[] courseParts = courseEntry.getKey().split(" - ");
                String courseInfo = courseParts[0];
                String semesterInfo = courseParts.length > 1 ? courseParts[1] : "";

                String[] courseInfoParts = courseInfo.split("\\s+", 2);
                String courseCode = courseInfoParts[0];
                String courseName = getCourseName(courseCode);

                String[] semesterParts = semesterInfo.split("\\s+", 2);
                String semester = semesterParts.length > 0 ? semesterParts[0] : "";
                String schoolYear = semesterParts.length > 1 ? semesterParts[1] : "";

                for (Map.Entry<String, Map<String, Long>> questionEntry : courseEntry.getValue().entrySet()) {
                    String question = questionEntry.getKey();

                    for (Map.Entry<String, Long> responseEntry : questionEntry.getValue().entrySet()) {
                        String response = responseEntry.getKey();
                        Long count = responseEntry.getValue();

                        // Get lecturer name from the course eval
                        String lecturerName = getLecturerNameForCourseAndQuestion(courseCode, question);

                        csvPrinter.printRecord(courseCode, courseName, semester, schoolYear,
                                lecturerName, question, response, count);
                    }
                }
            }

            csvPrinter.flush();
            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to export Likert responses to CSV", e);
        }
    }

    /**
     * Export open-ended responses to CSV
     * @return Path to the created CSV file
     */
    /*
    public String exportOpenResponses() {
        createExportDirectoryIfNotExists();
        String fileName = CSV_DIRECTORY + "/open_responses_" + System.currentTimeMillis() + ".csv";

        try (FileWriter fileWriter = new FileWriter(fileName);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT
                     .withHeader("Course Code", "Course Name", "Semester", "School Year",
                             "Lecturer", "Question", "Response"))) {

            Iterable<ResponseOpen> responses = responseOpenRepository.findAll();

            for (ResponseOpen response : responses) {
                CourseEval eval = response.getCourseEval();
                Classes course = eval.getCourse();
                Lecturer lecturer = eval.getLecturer();
                Questions question = response.getQuestion();

                csvPrinter.printRecord(
                        course.getClassCode().getcourseCode(),
                        course.getClassCode().getName(),
                        course.getSemester().getName(),
                        course.getSchoolYear().getName(),
                        lecturer.getFName() + " " + lecturer.getLName(),
                        question.getText(),
                        response.getResponse()
                );
            }

            csvPrinter.flush();
            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to export open-ended responses to CSV", e);
        }
    }

    /**
     * Export grade distribution by course
     * @return Path to the created CSV file
     */
    /*
    public String exportGradeDistribution() {
        createExportDirectoryIfNotExists();
        String fileName = CSV_DIRECTORY + "/grade_distribution_" + System.currentTimeMillis() + ".csv";

        try (FileWriter fileWriter = new FileWriter(fileName);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT
                     .withHeader("Course Code", "Course Name", "Semester", "School Year",
                             "Grade", "Count", "Retake Count", "Total Students"))) {

            Iterable<Grade> grades = gradeRepository.findAll();

            // Group by course and grade
            Map<String, Map<String, List<Grade>>> courseGradeMap = StreamSupport
                    .stream(grades.spliterator(), false)
                    .collect(Collectors.groupingBy(
                            grade -> {
                                Classes course = grade.getStudentClass();
                                return course.getClassCode().getcourseCode() + " - " +
                                        course.getClassCode().getName() + " - " +
                                        course.getSemester().getName() + " " +
                                        course.getSchoolYear().getName();
                            },
                            Collectors.groupingBy(Grade::getGrade)
                    ));

            for (String courseKey : courseGradeMap.keySet()) {
                String[] parts = courseKey.split(" - ");
                String courseCode = parts[0];
                String courseName = parts[1];
                String[] semesterYear = parts[2].split("\\s+", 2);
                String semester = semesterYear[0];
                String schoolYear = semesterYear[1];

                Map<String, List<Grade>> gradeMap = courseGradeMap.get(courseKey);
                int totalStudents = gradeMap.values().stream()
                        .mapToInt(List::size)
                        .sum();

                for (String grade : gradeMap.keySet()) {
                    List<Grade> gradeList = gradeMap.get(grade);
                    int count = gradeList.size();
                    long retakeCount = gradeList.stream()
                            .filter(Grade::isRetake)
                            .count();

                    csvPrinter.printRecord(
                            courseCode,
                            courseName,
                            semester,
                            schoolYear,
                            grade,
                            count,
                            retakeCount,
                            totalStudents
                    );
                }
            }

            csvPrinter.flush();
            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to export grade distribution to CSV", e);
        }
    }

    /**
     * Export course evaluation summary
     * @return Path to the created CSV file
     */

    /*
    public String exportEvaluationSummary() {
        createExportDirectoryIfNotExists();
        String fileName = CSV_DIRECTORY + "/evaluation_summary_" + System.currentTimeMillis() + ".csv";

        try (FileWriter fileWriter = new FileWriter(fileName);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT
                     .withHeader("Course Code", "Course Name", "Semester", "School Year",
                             "Lecturer", "Question", "Strongly Disagree", "Disagree",
                             "Neutral", "Agree", "Strongly Agree", "Average Score"))) {

            Iterable<ResponseLikert> responses = responseLikertRepository.findAll();

            // Group by course, lecturer, question
            Map<String, Map<String, Map<String, Map<String, Long>>>> groupedData = StreamSupport
                    .stream(responses.spliterator(), false)
                    .collect(Collectors.groupingBy(
                            response -> {
                                CourseEval eval = response.getCourseEval();
                                Classes course = eval.getCourse();
                                return course.getClassCode().getcourseCode() + " - " + course.getClassCode().getName();
                            },
                            Collectors.groupingBy(
                                    response -> {
                                        CourseEval eval = response.getCourseEval();
                                        Classes course = eval.getCourse();
                                        return course.getSemester().getName() + " " + course.getSchoolYear().getName();
                                    },
                                    Collectors.groupingBy(
                                            response -> {
                                                CourseEval eval = response.getCourseEval();
                                                Lecturer lecturer = eval.getLecturer();
                                                return lecturer.getFName() + " " + lecturer.getLName();
                                            },
                                            Collectors.groupingBy(
                                                    response -> response.getQuestion().getText(),
                                                    Collectors.counting()
                                            )
                                    )
                            )
                    ));


            // Define the likert scale options and their values for calculating average
            List<String> likertOptions = Arrays.asList(
                    "Strongly Disagree", "Disagree", "Neutral", "Agree", "Strongly Agree");
            Map<String, Integer> likertValues = new HashMap<>();
            likertValues.put("Strongly Disagree", 1);
            likertValues.put("Disagree", 2);
            likertValues.put("Neutral", 3);
            likertValues.put("Agree", 4);
            likertValues.put("Strongly Agree", 5);

            // Process and print the data
            for (Map.Entry<String, Map<String, Map<String, Map<String, Long>>>> courseEntry :
                    groupedData.entrySet()) {

                String[] courseParts = courseEntry.getKey().split(" - ");
                String courseCode = courseParts[0];
                String courseName = courseParts[1];

                for (Map.Entry<String, Map<String, Map<String, Long>>> semesterEntry :
                        courseEntry.getValue().entrySet()) {

                    String semesterYear = semesterEntry.getKey();
                    String[] semesterYearParts = semesterYear.split("\\s+", 2);
                    String semester = semesterYearParts[0];
                    String schoolYear = semesterYearParts.length > 1 ? semesterYearParts[1] : "";

                    for (Map.Entry<String, Map<String, Long>> lecturerEntry :
                            semesterEntry.getValue().entrySet()) {

                        String lecturer = lecturerEntry.getKey();

                        for (Map.Entry<String, Long> questionEntry :
                                lecturerEntry.getValue().entrySet()) {

                            String question = questionEntry.getKey();
                            Long responseCount = questionEntry.getValue();

                            // Calculate total responses and weighted sum for average
                            long totalResponses = responseCount;  // If it's just one value, no summing required
                            double weightedSum = 0;

                            // Prepare response counts for each Likert option
                            Map<String, Long> formattedResponses = new HashMap<>();
                            for (String option : likertOptions) {
                                // Assuming you want to format responses for each option
                                formattedResponses.put(option, responseCount); // Default, you can adjust it later
                                weightedSum += formattedResponses.get(option) * likertValues.getOrDefault(option, 0);
                            }

                            // Calculate average score
                            double averageScore = totalResponses > 0 ? weightedSum / totalResponses : 0;

                            // Print record
                            csvPrinter.printRecord(
                                    courseCode,
                                    courseName,
                                    semester,
                                    schoolYear,
                                    lecturer,
                                    question,
                                    formattedResponses.get("Strongly Disagree"),
                                    formattedResponses.get("Disagree"),
                                    formattedResponses.get("Neutral"),
                                    formattedResponses.get("Agree"),
                                    formattedResponses.get("Strongly Agree"),
                                    String.format("%.2f", averageScore)
                            );
                        }
                    }
                }
            }


            csvPrinter.flush();
            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to export evaluation summary to CSV", e);
        }
    }

    /**
     * Helper method to get course name from course code
     */

    /*
    private String getCourseName(String courseCode) {
        Optional<Course> course = courseRepository.findById(courseCode);
        return course.map(Course::getName).orElse("Unknown Course");
    }

    /**
     * Helper method to find lecturer name for a given course and question
     */

    /*
    private String getLecturerNameForCourseAndQuestion(String courseCode, String questionText) {
        // This is a simplified implementation - in a real system you would want to query this more efficiently
        Iterable<CourseEval> evals = courseEvalRepository.findAll();

        for (CourseEval eval : evals) {
            if (eval.getCourse().getClassCode().getcourseCode().equals(courseCode)) {
                Iterable<Questions> questions = questionsRepository.findAll();
                for (Questions question : questions) {
                    if (question.getText().equals(questionText) && question.getEval().getId().equals(eval.getId())) {
                        Lecturer lecturer = eval.getLecturer();
                        return lecturer.getFName() + " " + lecturer.getLName();
                    }
                }
            }
        }

        return "Unknown Lecturer";
    }
    */
}
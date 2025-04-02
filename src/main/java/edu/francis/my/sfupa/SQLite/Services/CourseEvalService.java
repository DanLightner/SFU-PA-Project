package edu.francis.my.sfupa.SQLite.Services;

import edu.francis.my.sfupa.SQLite.Models.*;
import edu.francis.my.sfupa.SQLite.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class CourseEvalService {

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    @Autowired
    private ClassesRepository classesRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private CourseRepository courseRepository;

    /**
     * Creates a new CourseEval entry with manually entered data.
     * @param courseCode Course code (must exist in the database)
     * @param semesterId Semester ID
     * @param schoolYearId School year ID
     * @param lecturerFirstName Lecturer's first name
     * @param lecturerLastName Lecturer's last name
     * @return The created CourseEval object or null if failed
     */
    public CourseEval createCourseEval(String courseCode, Long semesterId, Long schoolYearId,
                                       String lecturerFirstName, String lecturerLastName) {
        try {
            Lecturer lecturer = findOrCreateLecturer(lecturerFirstName, lecturerLastName);
            Optional<Course> courseOpt = courseRepository.findById(courseCode);
            if (courseOpt.isEmpty()) {
                System.out.println("Course not found: " + courseCode);
                return null;
            }
            Classes classEntity = findOrCreateClass(courseOpt.get(), semesterId, schoolYearId);
            if (classEntity == null) {
                return null;
            }
            CourseEval courseEval = new CourseEval();
            courseEval.setCourse(classEntity);
            courseEval.setLecturer(lecturer);
            return courseEvalRepository.save(courseEval);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves all course evaluations.
     * @return A list of CourseEval objects
     */
    public List<CourseEval> getAllCourseEvals() {
        return StreamSupport.stream(courseEvalRepository.findAll().spliterator(), false)
                .toList();
    }

    /**
     * Find a lecturer by name or create a new one if not found.
     */
    private Lecturer findOrCreateLecturer(String firstName, String lastName) {
        return StreamSupport.stream(lecturerRepository.findAll().spliterator(), false)
                .filter(lecturer -> lecturer.getFName().equalsIgnoreCase(firstName) &&
                        lecturer.getLName().equalsIgnoreCase(lastName))
                .findFirst()
                .orElseGet(() -> lecturerRepository.save(new Lecturer(firstName, lastName)));
    }

    /**
     * Finds or creates a class based on course, semester, and school year.
     */
    private Classes findOrCreateClass(Course course, Long semesterId, Long schoolYearId) {
        return StreamSupport.stream(classesRepository.findAll().spliterator(), false)
                .filter(classItem -> classItem.getClassCode().getcourseCode().equals(course.getcourseCode()) &&
                        Objects.equals(classItem.getSemester().getId(), semesterId) &&
                        Objects.equals(classItem.getSchoolYear().getIdSchoolYear(), schoolYearId))
                .findFirst()
                .orElse(null);
    }
}
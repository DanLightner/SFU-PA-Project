package edu.francis.my.sfupa.SQLite.Controller;

import edu.francis.my.sfupa.SQLite.Models.Course;
import edu.francis.my.sfupa.SQLite.Repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @PostMapping
    public Course addCourse(@RequestBody Course course) {
        return courseRepository.save(course);
    }

    @GetMapping
    public Iterable<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @GetMapping("/{id}")
    public Course getCourse(@PathVariable String courseCode) {
        return courseRepository.findById(courseCode).orElse(null);
    }
    //need to be changed, our pk is string courseCode

    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable String courseCode, @RequestBody Course courseDetails) {
        Optional<Course> optionalCourse = courseRepository.findById(courseCode);
        if (optionalCourse.isPresent()) {
            Course existingCourse = optionalCourse.get();
            existingCourse.setCourseId(courseDetails.getcourseCode());
            existingCourse.setName(courseDetails.getName());
            return courseRepository.save(existingCourse);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable String courseCode) {
        courseRepository.deleteById(courseCode);
    }

    /*
    To test the CRUD operations do this

    Send a post request to the following:
    http://localhost:8080/courses

    Add the following JSON data
    {
        "courseCode": "CS101",
        "name": "Introduction to Computer Science"
    }
    Then send the post request - If you want to see the information
    Go to http://localhost:8080/courses/1 and do a GET request


     */
}

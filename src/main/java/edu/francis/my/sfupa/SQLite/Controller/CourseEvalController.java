package edu.francis.my.sfupa.SQLite.Controller;

import edu.francis.my.sfupa.SQLite.Models.CourseEval;
import edu.francis.my.sfupa.SQLite.Services.CourseEvalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courseEvals")
public class CourseEvalController {

    @Autowired
    private CourseEvalService courseEvalService;

    // Endpoint to get all CourseEval records
    @GetMapping
    public List<CourseEval> getAllCourseEvals() {
        return courseEvalService.getAllCourseEvals();
    }

    // Endpoint to get a CourseEval by ID
    @GetMapping("/{id}")
    public Optional<CourseEval> getCourseEvalById(@PathVariable Integer id) {
        return courseEvalService.getCourseEvalById(id);
    }

    // Endpoint to create a new CourseEval
    @PostMapping
    public CourseEval createCourseEval(@RequestBody CourseEval courseEval) {
        return courseEvalService.saveCourseEval(courseEval);
    }

    // Endpoint to update an existing CourseEval by ID
    @PutMapping("/{id}")
    public CourseEval updateCourseEval(@PathVariable Integer id, @RequestBody CourseEval courseEval) {
        // Assuming the service handles checking if the courseEval with ID exists
        return courseEvalService.updateCourseEval(id, courseEval);
    }

    // Endpoint to delete a CourseEval by ID
    @DeleteMapping("/{id}")
    public void deleteCourseEval(@PathVariable Integer id) {
        courseEvalService.deleteCourseEval(id);
    }
}

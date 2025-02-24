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

    // Get all CourseEval records
    @GetMapping
    public List<CourseEval> getAllCourseEvals() {
        return courseEvalService.findAll();
    }

    // Get a CourseEval by ID
    @GetMapping("/{id}")
    public Optional<CourseEval> getCourseEvalById(@PathVariable Integer id) {
        return courseEvalService.findById(id);
    }

    // Create a new CourseEval
    @PostMapping
    public CourseEval createCourseEval(@RequestBody CourseEval courseEval) {
        return courseEvalService.save(courseEval);
    }

    // Update an existing CourseEval by ID
    @PutMapping("/{id}")
    public Optional<CourseEval> updateCourseEval(@PathVariable Integer id, @RequestBody CourseEval courseEval) {
        return courseEvalService.update(id, courseEval);
    }

    // Delete a CourseEval by ID
    @DeleteMapping("/{id}")
    public void deleteCourseEval(@PathVariable Integer id) {
        courseEvalService.delete(id);
    }


    /*
    If you want to test the CURD Operation for this code:

    Do the following (MAKE SURE YOU IMPLEMENTED A PROPER VALUE IN COURSE BEFORE)

     */
}

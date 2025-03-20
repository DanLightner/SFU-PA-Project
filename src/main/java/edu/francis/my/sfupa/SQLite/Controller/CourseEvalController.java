package edu.francis.my.sfupa.SQLite.Controller;

import edu.francis.my.sfupa.JavaFX.Controller.CourseEvalCsvImporter;
import edu.francis.my.sfupa.SQLite.Models.CourseEval;
import edu.francis.my.sfupa.SQLite.Services.CourseEvalService;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courseEvals")
public class CourseEvalController {

    @Autowired
    private CourseEvalService courseEvalService;
    @Autowired
    private CourseEvalCsvImporter courseEvalCsvImporter; // This injects the CSV importer



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
    // Upload CSV file to import course evaluations
    @PostMapping("/uploadCsv")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            courseEvalCsvImporter.importCsv(file); // Call your existing method to import CSV
            return ResponseEntity.ok("CSV file successfully uploaded and data imported!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing the CSV file: " + e.getMessage());
        }
    }
}


package edu.francis.my.sfupa.SQLite.Services;

import edu.francis.my.sfupa.SQLite.Models.CourseEval;
import edu.francis.my.sfupa.SQLite.Repository.CourseEvalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseEvalService {

    @Autowired
    private CourseEvalRepository courseEvalRepository;

    // Method to get all CourseEval records
    public List<CourseEval> getAllCourseEvals() {
        return (List<CourseEval>) courseEvalRepository.findAll();
    }

    // Method to get a CourseEval by ID
    public Optional<CourseEval> getCourseEvalById(Integer id) {
        return courseEvalRepository.findById(id);
    }

    // Method to save a CourseEval
    public CourseEval saveCourseEval(CourseEval courseEval) {
        return courseEvalRepository.save(courseEval);
    }

    // Method to update an existing CourseEval
    public CourseEval updateCourseEval(Integer id, CourseEval courseEval) {
        if (courseEvalRepository.existsById(id)) {
            courseEval.setId(id); // Assuming the entity has an 'id' field
            return courseEvalRepository.save(courseEval);
        }
        return null; // Or handle it better (e.g., throw exception)
    }

    // Method to delete a CourseEval by ID
    public void deleteCourseEval(Integer id) {
        courseEvalRepository.deleteById(id);
    }
}

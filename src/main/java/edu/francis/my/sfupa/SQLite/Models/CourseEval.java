package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class CourseEval {

    @Id
    private Integer id;
    private Integer rating;

    @ManyToOne
    private Course course;

    // Default constructor
    public CourseEval() {}

    // Constructor to initialize CourseEval
    public CourseEval(Integer id, Integer rating, Course course) {
        this.id = id;
        this.rating = rating;
        this.course = course;
    }

    // Getters and setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}

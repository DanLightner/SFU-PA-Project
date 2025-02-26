package edu.francis.my.sfupa.SQLite.Models;


import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "CourseEval")
public class CourseEval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer rating;//what is rating?

    @ManyToOne
    @JoinColumn(name = "course_id")  // This is the foreign key in the CourseEval table
    private Classes course;  // Reference to the Clsses entity (not courseCode)

    @ManyToOne
    @JoinColumn(name="lecturer_id")
    private Lecturer lecturer;

    public CourseEval() {}
    public CourseEval(Integer id, Integer rating, Classes course, Lecturer lecturer) {
        this.rating = rating;
        this.course = course;
        this.lecturer = lecturer;
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

    public Classes getCourse() {
        return course;
    }

    public void setCourse(Classes course) {
        this.course = course;
    }

    public Lecturer getLecturer() {return lecturer; }

    public void setLecturer(Lecturer lecturer) {this.lecturer = lecturer; }


}

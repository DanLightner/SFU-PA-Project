package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "Course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String courseCode;
    private String name;
    // Getters and Setters

    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }

    public String getcourseCode() {
        return courseCode;
    }
    public void setCourseId(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}

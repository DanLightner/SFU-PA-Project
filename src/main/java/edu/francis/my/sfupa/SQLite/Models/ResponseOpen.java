package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "ResponseOpen")
public class ResponseOpen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idResponse;

    @Lob
    @Column(nullable = false)
    private String response;

    @ManyToOne
    @JoinColumn(name = "CourseEval_idCourseEval", nullable = false)
    private CourseEval courseEval;

    @ManyToOne
    @JoinColumn(name= "Question_questionId", nullable = false)
    private Questions question;

    public ResponseOpen() {}
    public ResponseOpen(String response, CourseEval courseEval, Questions question) {
        this.response = response;
        this.courseEval = courseEval;
        this.question = question;
    }

    // Getters and Setters
    public Long getIdResponse() {
        return idResponse;
    }

    public void setIdResponse(Long idResponse) {
        this.idResponse = idResponse;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public CourseEval getCourseEval() {
        return courseEval;
    }

    public void setCourseEval(CourseEval courseEval) {
        this.courseEval = courseEval;
    }

    public Questions getQuestion() {return question;}

    public void setQuestion(Questions question) {this.question = question;}
}

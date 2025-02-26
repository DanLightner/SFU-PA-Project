package edu.francis.my.sfupa.SQLite.Models;

import jakarta.persistence.*;
import org.aspectj.weaver.patterns.TypePatternQuestions;

@Entity
@Table(name = "ResponseLikert")
public class ResponseLikert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idResponse;

    @Column(nullable = false)
    private String response; // Assuming ENUM is stored as a String

    @ManyToOne
    @JoinColumn(name = "CourseEval_idCourseEval", nullable = false)
    private CourseEval courseEval;

    @ManyToOne
    @JoinColumn(name= "Question_questionId", nullable = false)
    private Questions question;

    public ResponseLikert() {}
    public ResponseLikert(String response, CourseEval courseEval, Questions question) {
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

package edu.francis.my.sfupa.JavaFX.Controller;

import javafx.beans.property.SimpleStringProperty;

public class RetakeStudent {
    private final SimpleStringProperty studentId;
    private final SimpleStringProperty grade;

    public RetakeStudent(String studentId, String grade) {
        this.studentId = new SimpleStringProperty(studentId);
        this.grade = new SimpleStringProperty(grade);
    }

    public String getStudentId() { return studentId.get(); }
    public void setStudentId(String value) { studentId.set(value); }
    public SimpleStringProperty studentIdProperty() { return studentId; }
    
    public String getGrade() { return grade.get(); }
    public void setGrade(String value) { grade.set(value); }
    public SimpleStringProperty gradeProperty() { return grade; }
} 
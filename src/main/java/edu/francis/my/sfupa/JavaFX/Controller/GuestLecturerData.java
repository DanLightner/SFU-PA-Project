package edu.francis.my.sfupa.JavaFX.Controller;

public class GuestLecturerData {
    private String courseId;
    private String courseName;
    private String semester;
    private String year;

    public GuestLecturerData(String courseId, String courseName, String semester, String year) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.semester = semester;
        this.year = year;
    }

    // Getters and setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
} 
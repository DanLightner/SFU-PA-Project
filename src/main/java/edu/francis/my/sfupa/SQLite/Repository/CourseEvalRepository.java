package edu.francis.my.sfupa.SQLite.Repository;

import edu.francis.my.sfupa.SQLite.Models.CourseEval;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseEvalRepository extends CrudRepository<CourseEval, Integer> {
    // You can define custom query methods here if needed

    @Query("SELECT ce FROM CourseEval ce " +
           "WHERE ce.lecturer.id = :lecturerId " +
           "AND ce.lecturer IS NOT NULL " +
           "AND ce.course IS NOT NULL " +
           "AND ce.course.classCode IS NOT NULL " +
           "AND ce.course.semester IS NOT NULL " +
           "AND ce.course.schoolYear IS NOT NULL")
    Iterable<CourseEval> findValidEvaluationsByLecturerId(@Param("lecturerId") Long lecturerId);
    
    @Query("SELECT ce FROM CourseEval ce " +
           "WHERE ce.course.id = :courseId " +
           "AND ce.course IS NOT NULL " +
           "AND ce.course.classCode IS NOT NULL " +
           "AND ce.course.semester IS NOT NULL " +
           "AND ce.course.schoolYear IS NOT NULL")
    List<CourseEval> findByCourseId(@Param("courseId") Integer courseId);
}

package edu.francis.my.sfupa.SQLite.Repository;

import edu.francis.my.sfupa.SQLite.Models.Course;
import org.springframework.data.repository.CrudRepository;

public interface CourseRepository extends CrudRepository<Course, String> {
    Course findByName(String name);
    Course findByCourseCode(String courseCode);

}



package edu.francis.my.sfupa.SQLite.Repository;

import edu.francis.my.sfupa.SQLite.Models.Classes;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClassesRepository extends CrudRepository<Classes, Integer> {
    Optional<Classes> findByClassCode_CourseCodeAndSemester_IdAndSchoolYear_IdSchoolYear(String courseCode, int semesterId, Integer yearId);
}

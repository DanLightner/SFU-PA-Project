package edu.francis.my.sfupa.SQLite.Repository;

import edu.francis.my.sfupa.SQLite.Models.Grade;
import edu.francis.my.sfupa.SQLite.Models.Classes;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface GradeRepository extends CrudRepository<Grade, Integer> {
    List<Grade> findByStudentClass(Classes studentClass);
}

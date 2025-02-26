package edu.francis.my.sfupa.SQLite.Repository;

import edu.francis.my.sfupa.SQLite.Models.Student;
import org.springframework.data.repository.CrudRepository;

public interface StudentRepository extends CrudRepository<Student, Integer> {
}

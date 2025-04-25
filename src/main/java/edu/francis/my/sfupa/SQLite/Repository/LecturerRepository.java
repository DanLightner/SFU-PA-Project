package edu.francis.my.sfupa.SQLite.Repository;

import edu.francis.my.sfupa.SQLite.Models.Lecturer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface LecturerRepository extends CrudRepository<Lecturer, Integer> {
    
    @Query("SELECT l FROM Lecturer l WHERE l.Fname = :firstName AND l.Lname = :lastName")
    Lecturer findByFirstNameAndLastName(@Param("firstName") String firstName, @Param("lastName") String lastName);
}

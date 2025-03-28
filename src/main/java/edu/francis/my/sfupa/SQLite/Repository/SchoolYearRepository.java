package edu.francis.my.sfupa.SQLite.Repository;

import edu.francis.my.sfupa.SQLite.Models.SchoolYear;
import org.springframework.data.repository.CrudRepository;

public interface SchoolYearRepository extends CrudRepository<SchoolYear, Integer> {
    SchoolYear findByName(String name);
}

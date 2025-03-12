package edu.francis.my.sfupa.SQLite.Repository;

import edu.francis.my.sfupa.SQLite.Models.Questions;
import org.springframework.data.repository.CrudRepository;

public interface QuestionsRepository extends CrudRepository<Questions, Integer> {
}

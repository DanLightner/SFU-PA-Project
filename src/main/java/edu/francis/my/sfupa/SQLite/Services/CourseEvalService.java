package edu.francis.my.sfupa.SQLite.Services;

import edu.francis.my.sfupa.SQLite.Models.CourseEval;
import edu.francis.my.sfupa.SQLite.Repository.CourseEvalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourseEvalService extends AbstractGenericService<CourseEval, Integer> {

    @Autowired
    public CourseEvalService(CourseEvalRepository courseEvalRepository) {
        super(courseEvalRepository);
    }
}

package edu.francis.my.sfupa.repository;
import edu.francis.my.sfupa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>
{
    User findByUsername(String username);
}




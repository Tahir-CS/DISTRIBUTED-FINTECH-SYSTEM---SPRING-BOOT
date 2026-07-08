package.com.example.crudapi.repository

import com.exampple.crudapi.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional



public class UserRepository extends JpaRepository(User,Long){


    Optional<User>findByUsername(string username);
    boolean existsByUsername(String username);
    
}
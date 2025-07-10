package grupo6.umbook.repository;

import grupo6.umbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring Data JPA creará la consulta automáticamente
    User findByEmail(String email);
}
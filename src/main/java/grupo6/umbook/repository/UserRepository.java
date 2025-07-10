package grupo6.umbook.repository;

import grupo6.umbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByNameContaining(@Param("searchTerm") String searchTerm);

    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.birthDate IS NOT NULL AND FUNCTION('MONTH', u.birthDate) = FUNCTION('MONTH', :date) AND FUNCTION('DAY', u.birthDate) = FUNCTION('DAY', :date)")
    List<User> findUsersWithBirthdayOn(@Param("date") LocalDate date);

    List<User> findByEnabledTrueAndBirthDateIsNotNull();

    // La lógica para filtrar cumpleaños en los próximos n días se recomienda implementarla en el servicio.
}

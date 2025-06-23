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

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByNameContaining(@Param("searchTerm") String searchTerm);

    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.birthDate IS NOT NULL AND FUNCTION('MONTH', u.birthDate) = FUNCTION('MONTH', :date) AND FUNCTION('DAY', u.birthDate) = FUNCTION('DAY', :date)")
    List<User> findUsersWithBirthdayOn(@Param("date") LocalDate date);

    // Custom implementation to find users with birthdays in the next N days
    // This avoids complex JPQL functions that might not be compatible across different databases
    List<User> findByEnabledTrueAndBirthDateIsNotNull();

    // The actual filtering for birthdays in next days will be done in the service layer
}

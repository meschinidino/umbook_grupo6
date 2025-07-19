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

    // Un nombre o apellido no es único, por lo que debe devolver una lista.
    List<User> findByFirstName(String firstname);
    List<User> findByLastName(String lastname);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByNameContaining(@Param("searchTerm") String searchTerm);

    // El comentario sobre implementar la lógica de cumpleaños en el servicio es una excelente práctica.
    // Este método obtiene todos los usuarios con fecha de nacimiento para ser filtrados en la capa de servicio.
    @Query("SELECT u FROM User u WHERE u.enabled = true AND u.birthDate IS NOT NULL")
    List<User> findAllActiveUsersWithBirthDate();
}
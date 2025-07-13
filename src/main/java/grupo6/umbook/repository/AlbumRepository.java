package grupo6.umbook.repository;

import grupo6.umbook.model.Album;
import grupo6.umbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    // Después (para ignorar los eliminados)
    @Query("SELECT a FROM Album a WHERE a.owner = :owner AND a.state != 'ELIMINADO'")
    List<Album> findByOwner(@Param("owner") User owner);

    Optional<Album> findByNameAndOwner(String name, User owner);

    boolean existsByNameAndOwner(String name, User owner);

    @Query("SELECT a FROM Album a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Album> findByNameOrDescriptionContaining(@Param("searchTerm") String searchTerm);

    @Query("SELECT a FROM Album a WHERE a.owner = :owner ORDER BY a.createdAt DESC")
    List<Album> findByOwnerOrderByCreatedAtDesc(@Param("owner") User owner);

    @Query("SELECT COUNT(p) FROM Photo p WHERE p.album.id = :albumId")
    long countPhotosByAlbumId(@Param("albumId") Long albumId);

}
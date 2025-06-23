package grupo6.umbook.repository;

import grupo6.umbook.model.Album;
import grupo6.umbook.model.Photo;
import grupo6.umbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    List<Photo> findByAlbum(Album album);

    List<Photo> findByUploader(User uploader);

    @Query("SELECT p FROM Photo p WHERE p.album.id = :albumId ORDER BY p.uploadedAt DESC")
    List<Photo> findByAlbumIdOrderByUploadedAtDesc(@Param("albumId") Long albumId);

    @Query("SELECT p FROM Photo p WHERE p.album.owner.id = :userId ORDER BY p.uploadedAt DESC")
    List<Photo> findByAlbumOwnerIdOrderByUploadedAtDesc(@Param("userId") Long userId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Photo p WHERE p.album.id = :albumId AND p.fileName = :fileName")
    boolean existsByAlbumIdAndFileName(@Param("albumId") Long albumId, @Param("fileName") String fileName);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.photo.id = :photoId")
    long countCommentsByPhotoId(@Param("photoId") Long photoId);

    Optional<Photo> findByIdAndAlbumId(Long id, Long albumId);
}
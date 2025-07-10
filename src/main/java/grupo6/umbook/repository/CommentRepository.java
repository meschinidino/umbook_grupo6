package grupo6.umbook.repository;

import grupo6.umbook.model.Comment;
import grupo6.umbook.model.Photo;
import grupo6.umbook.model.Post;
import grupo6.umbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByAuthor(User author);

    List<Comment> findByPhoto(Photo photo);

    List<Comment> findByPost(Post post);

    @Query("SELECT c FROM Comment c WHERE c.photo.id = :photoId ORDER BY c.createdAt DESC")
    List<Comment> findByPhotoIdOrderByCreatedAtDesc(@Param("photoId") Long photoId);

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
    List<Comment> findByPostIdOrderByCreatedAtDesc(@Param("postId") Long postId);

    @Query("SELECT c FROM Comment c WHERE c.author.id = :userId ORDER BY c.createdAt DESC")
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT c FROM Comment c WHERE c.photo.album.owner.id = :userId ORDER BY c.createdAt DESC")
    List<Comment> findCommentsOnUserPhotos(@Param("userId") Long userId);

    @Query("SELECT c FROM Comment c WHERE c.post.wallOwner.id = :userId ORDER BY c.createdAt DESC")
    List<Comment> findCommentsOnUserWall(@Param("userId") Long userId);

    @Query("SELECT c FROM Comment c WHERE c.deleted = false AND (c.photo.id = :photoId OR c.post.id = :postId) ORDER BY c.createdAt DESC")
    List<Comment> findActiveCommentsByPhotoIdOrPostId(@Param("photoId") Long photoId, @Param("postId") Long postId);
}
package grupo6.umbook.repository;

import grupo6.umbook.model.Group;
import grupo6.umbook.model.Post;
import grupo6.umbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByAuthor(User author);

    List<Post> findByWallOwner(User wallOwner);

    List<Post> findByGroup(Group group);

    @Query("SELECT p FROM Post p WHERE p.wallOwner.id = :userId ORDER BY p.createdAt DESC")
    List<Post> findByWallOwnerIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT p FROM Post p WHERE p.group.id = :groupId ORDER BY p.createdAt DESC")
    List<Post> findByGroupIdOrderByCreatedAtDesc(@Param("groupId") Long groupId);

    @Query("SELECT p FROM Post p WHERE p.author.id = :userId ORDER BY p.createdAt DESC")
    List<Post> findByAuthorIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT p FROM Post p WHERE p.deleted = false AND p.wallOwner.id = :userId ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByWallOwnerId(@Param("userId") Long userId);

    @Query("SELECT p FROM Post p WHERE p.deleted = false AND p.group.id = :groupId ORDER BY p.createdAt DESC")
    List<Post> findActivePostsByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT p FROM Post p WHERE p.deleted = false AND (p.wallOwner.id IN :friendIds OR p.author.id IN :friendIds) ORDER BY p.createdAt DESC")
    List<Post> findFeedPostsByFriendIds(@Param("friendIds") List<Long> friendIds);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countCommentsByPostId(@Param("postId") Long postId);
}
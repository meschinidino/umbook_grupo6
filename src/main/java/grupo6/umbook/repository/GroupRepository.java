package grupo6.umbook.repository;

import grupo6.umbook.model.Group;
import grupo6.umbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByName(String name);

    boolean existsByName(String name);

    List<Group> findByCreator(User creator);

    @Query("SELECT g FROM Group g WHERE :user MEMBER OF g.members")
    List<Group> findGroupsByMember(@Param("user") User user);

    @Query("SELECT g FROM Group g WHERE g.visibility = grupo6.umbook.model.GroupVisibility.PUBLIC")
    List<Group> findPublicGroups();

    @Query("SELECT g FROM Group g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(g.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Group> findByNameOrDescriptionContaining(@Param("searchTerm") String searchTerm);

    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END FROM Group g WHERE g.id = :groupId AND :user MEMBER OF g.members")
    boolean isUserMemberOfGroup(@Param("groupId") Long groupId, @Param("user") User user);
}
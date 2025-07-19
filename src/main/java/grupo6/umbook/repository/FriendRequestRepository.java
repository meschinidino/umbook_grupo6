package grupo6.umbook.repository;

import grupo6.umbook.model.FriendRequest;
import grupo6.umbook.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {

    List<FriendRequest> findBySender(User sender);

    List<FriendRequest> findByReceiver(User receiver);

    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequest.FriendRequestStatus status);

    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);

    boolean existsBySenderAndReceiver(User sender, User receiver);

    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequest.FriendRequestStatus status);
}
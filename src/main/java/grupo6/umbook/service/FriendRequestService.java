package grupo6.umbook.service;

import grupo6.umbook.model.FriendRequest;
import grupo6.umbook.model.Notification;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.FriendRequestRepository;
import grupo6.umbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public FriendRequestService(
            FriendRequestRepository friendRequestRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public FriendRequest sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

        // Check if they are already friends
        if (sender.getFriends().contains(receiver)) {
            throw new IllegalArgumentException("Users are already friends");
        }

        // Check if a request already exists
        if (friendRequestRepository.existsBySenderAndReceiver(sender, receiver)) {
            throw new IllegalArgumentException("Friend request already sent");
        }

        // Check if there's a pending request in the opposite direction
        Optional<FriendRequest> oppositeRequest = friendRequestRepository.findBySenderAndReceiver(receiver, sender);
        if (oppositeRequest.isPresent() && oppositeRequest.get().getStatus() == FriendRequest.FriendRequestStatus.PENDING) {
            // Accept the opposite request instead of creating a new one
            return acceptFriendRequest(oppositeRequest.get().getId(), senderId);
        }

        FriendRequest friendRequest = new FriendRequest(sender, receiver);
        FriendRequest savedRequest = friendRequestRepository.save(friendRequest);

        // Create notification for the receiver using NotificationService
        notificationService.createNotification(
                receiver.getId(),
                sender.getFirstName() + " " + sender.getLastName() + " sent you a friend request",
                Notification.NotificationType.FRIEND_REQUEST,
                savedRequest.getId()
        );

        return savedRequest;
    }

    @Transactional
    public FriendRequest acceptFriendRequest(Long requestId, Long receiverId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new IllegalArgumentException("Only the receiver can accept the request");
        }

        if (request.getStatus() != FriendRequest.FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Request is not pending");
        }

        // Update request status
        request.setStatus(FriendRequest.FriendRequestStatus.ACCEPTED);
        request.setUpdatedAt(LocalDateTime.now());

        // Add users as friends
        User sender = request.getSender();
        User receiver = request.getReceiver();
        sender.addFriend(receiver);

        // Save changes
        userRepository.save(sender);
        userRepository.save(receiver);
        FriendRequest savedRequest = friendRequestRepository.save(request);

        // Create notification for the sender using NotificationService
        notificationService.createNotification(
                sender.getId(),
                receiver.getFirstName() + " " + receiver.getLastName() + " accepted your friend request",
                Notification.NotificationType.FRIEND_ACCEPTED,
                savedRequest.getId()
        );

        return savedRequest;
    }

    @Transactional
    public FriendRequest rejectFriendRequest(Long requestId, Long receiverId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Friend request not found"));

        if (!request.getReceiver().getId().equals(receiverId)) {
            throw new IllegalArgumentException("Only the receiver can reject the request");
        }

        if (request.getStatus() != FriendRequest.FriendRequestStatus.PENDING) {
            throw new IllegalArgumentException("Request is not pending");
        }

        // Update request status
        request.setStatus(FriendRequest.FriendRequestStatus.REJECTED);
        request.setUpdatedAt(LocalDateTime.now());

        return friendRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<FriendRequest> getPendingRequestsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return friendRequestRepository.findByReceiverAndStatus(user, FriendRequest.FriendRequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<FriendRequest> getSentRequestsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return friendRequestRepository.findBySender(user);
    }

    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Cannot remove yourself as a friend");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new IllegalArgumentException("Friend not found"));

        if (!user.getFriends().contains(friend)) {
            throw new IllegalArgumentException("Users are not friends");
        }

        user.removeFriend(friend);
        userRepository.save(user);
        userRepository.save(friend);
    }
}
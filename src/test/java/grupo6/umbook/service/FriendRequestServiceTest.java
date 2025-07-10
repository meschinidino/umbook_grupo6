package grupo6.umbook.service;

import grupo6.umbook.model.FriendRequest;
import grupo6.umbook.model.Notification;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.FriendRequestRepository;
import grupo6.umbook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FriendRequestServiceTest {

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FriendRequestService friendRequestService;

    private User sender;
    private User receiver;
    private FriendRequest friendRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up test users
        sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");
        sender.setFirstName("John");
        sender.setLastName("Doe");
        sender.setEmail("john.doe@example.com");
        sender.setEnabled(true);

        receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");
        receiver.setFirstName("Jane");
        receiver.setLastName("Smith");
        receiver.setEmail("jane.smith@example.com");
        receiver.setEnabled(true);

        // Set up test friend request
        friendRequest = new FriendRequest(sender, receiver);
        friendRequest.setId(1L);
        friendRequest.setStatus(FriendRequest.FriendRequestStatus.PENDING);
    }

    @Test
    void sendFriendRequest_Success() {
        // Arrange
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        when(friendRequestRepository.existsBySenderAndReceiver(sender, receiver)).thenReturn(false);
        when(friendRequestRepository.findBySenderAndReceiver(receiver, sender)).thenReturn(Optional.empty());
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(friendRequest);
        when(notificationService.createNotification(
                eq(receiver.getId()),
                anyString(),
                eq(Notification.NotificationType.FRIEND_REQUEST),
                anyLong()
        )).thenReturn(new Notification());

        // Act
        FriendRequest result = friendRequestService.sendFriendRequest(sender.getId(), receiver.getId());

        // Assert
        assertNotNull(result);
        assertEquals(sender, result.getSender());
        assertEquals(receiver, result.getReceiver());
        assertEquals(FriendRequest.FriendRequestStatus.PENDING, result.getStatus());
        
        // Verify notification was created
        verify(notificationService, times(1)).createNotification(
                eq(receiver.getId()),
                anyString(),
                eq(Notification.NotificationType.FRIEND_REQUEST),
                anyLong()
        );
    }

    @Test
    void sendFriendRequest_SameUser() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            friendRequestService.sendFriendRequest(sender.getId(), sender.getId());
        });

        assertEquals("Cannot send friend request to yourself", exception.getMessage());
        verify(friendRequestRepository, never()).save(any(FriendRequest.class));
        verify(notificationService, never()).createNotification(anyLong(), anyString(), any(), anyLong());
    }

    @Test
    void sendFriendRequest_AlreadyFriends() {
        // Arrange
        when(userRepository.findById(sender.getId())).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiver.getId())).thenReturn(Optional.of(receiver));
        
        // Make them friends
        sender.getFriends().add(receiver);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            friendRequestService.sendFriendRequest(sender.getId(), receiver.getId());
        });

        assertEquals("Users are already friends", exception.getMessage());
        verify(friendRequestRepository, never()).save(any(FriendRequest.class));
        verify(notificationService, never()).createNotification(anyLong(), anyString(), any(), anyLong());
    }

    @Test
    void acceptFriendRequest_Success() {
        // Arrange
        when(friendRequestRepository.findById(friendRequest.getId())).thenReturn(Optional.of(friendRequest));
        when(friendRequestRepository.save(any(FriendRequest.class))).thenReturn(friendRequest);
        when(userRepository.save(any(User.class))).thenReturn(null); // Not using the return value
        when(notificationService.createNotification(
                eq(sender.getId()),
                anyString(),
                eq(Notification.NotificationType.FRIEND_ACCEPTED),
                anyLong()
        )).thenReturn(new Notification());

        // Act
        FriendRequest result = friendRequestService.acceptFriendRequest(friendRequest.getId(), receiver.getId());

        // Assert
        assertNotNull(result);
        assertEquals(FriendRequest.FriendRequestStatus.ACCEPTED, result.getStatus());
        
        // Verify users were saved
        verify(userRepository, times(2)).save(any(User.class));
        
        // Verify notification was created
        verify(notificationService, times(1)).createNotification(
                eq(sender.getId()),
                anyString(),
                eq(Notification.NotificationType.FRIEND_ACCEPTED),
                anyLong()
        );
    }

    @Test
    void acceptFriendRequest_NotPending() {
        // Arrange
        friendRequest.setStatus(FriendRequest.FriendRequestStatus.ACCEPTED);
        when(friendRequestRepository.findById(friendRequest.getId())).thenReturn(Optional.of(friendRequest));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            friendRequestService.acceptFriendRequest(friendRequest.getId(), receiver.getId());
        });

        assertEquals("Request is not pending", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).createNotification(anyLong(), anyString(), any(), anyLong());
    }

    @Test
    void acceptFriendRequest_WrongReceiver() {
        // Arrange
        when(friendRequestRepository.findById(friendRequest.getId())).thenReturn(Optional.of(friendRequest));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            friendRequestService.acceptFriendRequest(friendRequest.getId(), sender.getId());
        });

        assertEquals("Only the receiver can accept the request", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(notificationService, never()).createNotification(anyLong(), anyString(), any(), anyLong());
    }
}
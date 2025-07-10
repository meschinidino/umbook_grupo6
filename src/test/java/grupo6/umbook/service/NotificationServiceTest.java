package grupo6.umbook.service;

import grupo6.umbook.model.Notification;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.NotificationRepository;
import grupo6.umbook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("Pablo");
        testUser.setLastName("Gomez");
        testUser.setEmail("pgomez@gmail.com");
        testUser.setEnabled(true);

        // Set up test notification
        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUser(testUser);
        testNotification.setMessage("Test notification message");
        testNotification.setType(Notification.NotificationType.NEW_COMMENT);
        testNotification.setReferenceId(123L);
        testNotification.setRead(false);
    }

    @Test
    void createNotification_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.createNotification(
                testUser.getId(),
                "Test notification message",
                Notification.NotificationType.NEW_COMMENT
        );

        // Assert
        assertNotNull(result);
        assertEquals(testNotification.getMessage(), result.getMessage());
        assertEquals(testNotification.getType(), result.getType());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void createNotification_WithReferenceId_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.createNotification(
                testUser.getId(),
                "Test notification message",
                Notification.NotificationType.NEW_COMMENT,
                123L
        );

        // Assert
        assertNotNull(result);
        assertEquals(testNotification.getMessage(), result.getMessage());
        assertEquals(testNotification.getType(), result.getType());
        assertEquals(testNotification.getReferenceId(), result.getReferenceId());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void createNotification_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.createNotification(
                    999L,
                    "Test notification message",
                    Notification.NotificationType.NEW_COMMENT
            );
        });

        assertEquals("User not found", exception.getMessage());
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void getNotificationsForUser_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(anyLong())).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getNotificationsForUser(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testNotification.getMessage(), result.get(0).getMessage());
        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(testUser.getId());
    }

    @Test
    void getUnreadNotificationsForUser_Success() {
        // Arrange
        List<Notification> notifications = Arrays.asList(testNotification);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(notificationRepository.findUnreadByUserId(anyLong())).thenReturn(notifications);

        // Act
        List<Notification> result = notificationService.getUnreadNotificationsForUser(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testNotification.getMessage(), result.get(0).getMessage());
        assertFalse(result.get(0).isRead());
        verify(notificationRepository, times(1)).findUnreadByUserId(testUser.getId());
    }

    @Test
    void countUnreadNotificationsForUser_Success() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(notificationRepository.countUnreadByUserId(anyLong())).thenReturn(5L);

        // Act
        long result = notificationService.countUnreadNotificationsForUser(testUser.getId());

        // Assert
        assertEquals(5L, result);
        verify(notificationRepository, times(1)).countUnreadByUserId(testUser.getId());
    }

    @Test
    void markAsRead_Success() {
        // Arrange
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        Notification result = notificationService.markAsRead(testNotification.getId(), testUser.getId());

        // Assert
        assertNotNull(result);
        assertTrue(result.isRead());
        assertNotNull(result.getReadAt());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void markAsRead_NotificationNotFound() {
        // Arrange
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(999L, testUser.getId());
        });

        assertEquals("Notification not found", exception.getMessage());
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void markAsRead_WrongUser() {
        // Arrange
        when(notificationRepository.findById(anyLong())).thenReturn(Optional.of(testNotification));

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            notificationService.markAsRead(testNotification.getId(), 999L);
        });

        assertEquals("Notification does not belong to this user", exception.getMessage());
        verify(notificationRepository, never()).save(any(Notification.class));
    }
}
package grupo6.umbook.service;

import grupo6.umbook.model.User;
import grupo6.umbook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("pgomez");
        testUser.setPassword("jhklejdnWED23");
        testUser.setFirstName("Pablo");
        testUser.setLastName("Gomez");
        testUser.setEmail("pgomez@gmail.com");
        testUser.setEnabled(true);
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.registerUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateUsername() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(testUser);
        });

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(testUser);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.authenticate("pgomez", "jhklejdnWED23");

        // Assert
        assertTrue(result);
    }

    @Test
    void authenticate_WrongPassword() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.authenticate("pgomez", "wrongPassword");

        // Assert
        assertFalse(result);
    }

    @Test
    void authenticate_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Act
        boolean result = userService.authenticate("nonexistentUser", "anyPassword");

        // Assert
        assertFalse(result);
    }

    @Test
    void authenticate_DisabledUser() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.authenticate("pgomez", "jhklejdnWED23");

        // Assert
        assertFalse(result);
    }

    @Test
    void findByUsername_Success() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByUsername("pgomez");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
    }

    @Test
    void findByEmail_Success() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.findByEmail("pgomez@gmail.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getEmail(), result.get().getEmail());
    }
}
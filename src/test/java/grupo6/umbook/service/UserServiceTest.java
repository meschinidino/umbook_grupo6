package grupo6.umbook.service;

import grupo6.umbook.model.User;
import grupo6.umbook.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
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
        testUser.setPassword("jhklejdnWED23");
        testUser.setFirstName("Pablo");
        testUser.setLastName("Gomez");
        testUser.setEmail("pgomez@gmail.com");
        testUser.setEnabled(true);
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.registerUser(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_DuplicateEmail() {
        // Arrange
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
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.authenticate("pgomez@gmail.com", "jhklejdnWED23");

        // Assert
        assertTrue(result);
    }

    @Test
    void authenticate_WrongPassword() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.authenticate("pgomez", "wrongPassword");

        // Assert
        assertFalse(result);
    }

    @Test
    void authenticate_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        boolean result = userService.authenticate("nonexistentUser", "anyPassword");

        // Assert
        assertFalse(result);
    }

    @Test
    void authenticate_DisabledUser() {
        // Arrange
        testUser.setEnabled(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        boolean result = userService.authenticate("pgomez@gmail.com", "jhklejdnWED23");

        // Assert
        assertFalse(result);
    }

    @Test
    void findByFirstName_Success() {
        // Arrange: Preparamos los datos
        // 1. Creamos una lista que contiene nuestro usuario de prueba.
        List<User> usersFound = List.of(testUser);

        // 2. Le decimos a Mockito que cuando se llame a userRepository.findByFirstName,
        //    debe devolver la lista que acabamos de crear.
        when(userRepository.findByFirstName(anyString())).thenReturn(usersFound);

        // Act: Ejecutamos el método que queremos probar
        List<User> result = userService.findByFirstName("Pablo");

        assertNotNull(result); // La lista no debe ser nula
        assertFalse(result.isEmpty()); // La lista no debe estar vacía
        assertEquals(1, result.size()); // Esperamos un solo usuario en la lista
        assertEquals("Pablo", result.get(0).getFirstName()); // Verificamos que es el usuario correcto
    }

    @Test
    void findByFirstName_NotFound() {
        // Arrange: Simulamos que el repositorio no encuentra a nadie
        // Le decimos a Mockito que devuelva una lista vacía.
        when(userRepository.findByFirstName(anyString())).thenReturn(Collections.emptyList());

        // Act: Ejecutamos el método
        List<User> result = userService.findByFirstName("NombreInexistente");
        
        assertNotNull(result); // La lista no debe ser nula
        assertTrue(result.isEmpty()); // La lista SÍ debe estar vacía
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
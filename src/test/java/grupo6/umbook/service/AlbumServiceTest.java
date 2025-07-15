package grupo6.umbook.service;

import grupo6.umbook.model.Album;
import grupo6.umbook.model.Group;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.AlbumRepository;
import grupo6.umbook.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

    @Mock
    private UserRepository userRepository; // Mock del repositorio de usuarios

    @Mock
    private AlbumRepository albumRepository; // Mock del repositorio de álbumes

    @InjectMocks
    private AlbumService albumService; // La instancia del servicio que vamos a probar

    @Test
    void canUserViewAlbum_WhenUserIsOwner_ShouldReturnTrue() {
        // 1. Given (Preparación)
        User owner = new User();
        owner.setId(1L);

        Album album = new Album();
        album.setOwner(owner);

        // 2. When (Acción)
        boolean hasPermission = albumService.canUserViewAlbum(album, owner);

        // 3. Then (Verificación)
        assertTrue(hasPermission, "El dueño del álbum debería tener permiso para verlo.");
    }

    @Test
    void canUserViewAlbum_WhenUserIsInPermittedGroup_ShouldReturnTrue() {
        // 1. Given (Preparación)
        User owner = new User();
        owner.setId(1L);

        User viewer = new User();
        viewer.setId(2L);

        Group permittedGroup = new Group();
        permittedGroup.setId(10L);

        viewer.setGroups(Set.of(permittedGroup)); // El que ve pertenece al grupo

        Album album = new Album("Álbum privado", "desc", owner);
        album.setPermittedToView(Set.of(permittedGroup)); // El grupo tiene permiso

        // 2. When (Acción)
        boolean hasPermission = albumService.canUserViewAlbum(album, viewer);

        // 3. Then (Verificación)
        assertTrue(hasPermission, "Un miembro de un grupo con permiso debería poder ver el álbum.");
    }

    @Test
    void canUserViewAlbum_WhenUserIsNotOwnerOrInPermittedGroup_ShouldReturnFalse() {
        // 1. Given (Preparación)
        User owner = new User();
        owner.setId(1L);

        User viewer = new User();
        viewer.setId(2L);

        Group permittedGroup = new Group();
        permittedGroup.setId(10L);
        Group otherGroup = new Group();
        otherGroup.setId(11L);

        viewer.setGroups(Set.of(otherGroup)); // El que ve pertenece a un grupo sin permiso

        Album album = new Album("Álbum privado", "desc", owner);
        album.setPermittedToView(Set.of(permittedGroup)); // El permiso es para otro grupo

        // 2. When (Acción)
        boolean hasPermission = albumService.canUserViewAlbum(album, viewer);

        // 3. Then (Verificación)
        assertFalse(hasPermission, "Un usuario sin permisos no debería poder ver el álbum.");
    }

    @Test
    void canUserViewAlbum_WhenUserIsNotLoggedIn_ShouldReturnFalse() {
        // 1. Given (Preparación)
        User owner = new User();
        owner.setId(1L);

        Album album = new Album("Álbum privado", "desc", owner);
        album.setPermittedToView(Set.of(new Group())); // El álbum es privado

        User viewer = null; // Simulamos un usuario no logueado

        // 2. When (Acción)
        boolean hasPermission = albumService.canUserViewAlbum(album, viewer);

        // 3. Then (Verificación)
        assertFalse(hasPermission, "Un usuario no logueado no debería poder ver un álbum privado.");
    }
}
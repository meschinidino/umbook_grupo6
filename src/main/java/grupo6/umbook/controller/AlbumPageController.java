package grupo6.umbook.controller;

import grupo6.umbook.dto.CreateAlbumRequest;
import grupo6.umbook.model.Album;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.UserRepository;
import grupo6.umbook.service.AlbumService;
import grupo6.umbook.service.GroupService;
import grupo6.umbook.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;

@Controller
public class AlbumPageController {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private GroupService groupService; // Inyectamos el servicio de grupos

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PhotoService photoService;

    /**
     * Muestra la página principal de álbumes del usuario.
     */
    @GetMapping("/albums")
    public String showAlbumsPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("Usuario logueado no encontrado."));
            model.addAttribute("albums", albumService.findAlbumsByOwnerId(currentUser.getId()));
        } else {
            model.addAttribute("albums", Collections.emptyList());
        }
        return "albums";
    }

    /**
     * Muestra el formulario para crear un nuevo álbum.
     */
    @GetMapping("/albums/create")
    public String showCreateAlbumPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario logueado no encontrado."));

        // Buscamos los grupos del usuario y los pasamos a la vista
        model.addAttribute("userGroups", groupService.findGroupsByMember(currentUser.getId()));
        model.addAttribute("albumRequest", new CreateAlbumRequest());

        return "create_album"; // Asegúrate de que tu archivo HTML se llame así
    }

    /**
     * Procesa los datos del formulario para crear un nuevo álbum.
     */
    @PostMapping("/albums/create")
    public String handleCreateAlbum(@ModelAttribute CreateAlbumRequest albumRequest, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String userEmail = authentication.getName();
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario logueado no encontrado."));

        // Llamamos al servicio para crear el álbum con los datos del DTO
        albumService.createAlbum(albumRequest, creator.getId());

        return "redirect:/albums";
    }

    @GetMapping("/albums/{albumId}")
    public String showAlbumDetailPage(@PathVariable Long albumId, Model model, Authentication authentication) {

        // Buscamos el álbum específico que se quiere ver
        Album currentAlbum = albumService.findById(albumId);
        model.addAttribute("album", currentAlbum);

        // También buscamos todos los álbumes del usuario para la navegación lateral
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
            model.addAttribute("ownerAlbums", albumService.findAlbumsByOwnerId(currentUser.getId()));
        }

        model.addAttribute("activePage", "albums");
        return "album_detail"; // Renderiza el nuevo archivo album-detail.html
    }

    /**
     * AÑADIDO: Muestra el formulario para subir una nueva foto a un álbum específico.
     */
    @GetMapping("/albums/{albumId}/upload")
    public String showUploadPhotoPage(@PathVariable Long albumId, Model model) {
        model.addAttribute("albumId", albumId); // Pasamos el ID del álbum a la vista
        return "upload_photo"; // Renderiza el nuevo archivo upload-photo.html
    }

    /**
     * AÑADIDO: Procesa el envío del formulario de subida de fotos.
     */
    @PostMapping("/albums/{albumId}/upload")
    public String handleUploadPhoto(
            @PathVariable Long albumId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description,
            Authentication authentication) throws IOException {

        // Obtenemos el ID del usuario que sube la foto
        String userEmail = authentication.getName();
        User uploader = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // Llamamos al servicio que ya tenías para subir la foto
        photoService.uploadPhoto(file, description, albumId, uploader.getId());

        // Redirigimos de vuelta a la página del álbum
        return "redirect:/albums/" + albumId;
    }

    /**
     * AÑADIDO: Maneja la petición para eliminar una foto.
     */
    @GetMapping("/photos/{photoId}/delete")
    public String handleDeletePhoto(
            @PathVariable Long photoId,
            @RequestParam Long albumId, // Necesitamos el ID del álbum para saber a dónde volver
            Authentication authentication) {

        // Obtenemos el ID del usuario que realiza la acción para la validación de permisos
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // Llamamos al servicio para que borre la foto
        photoService.deletePhoto(photoId, currentUser.getId());

        // Redirigimos de vuelta a la página del álbum actualizada
        return "redirect:/albums/" + albumId;
    }

}
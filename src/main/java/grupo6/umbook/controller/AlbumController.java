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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Controller
public class AlbumController {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private GroupService groupService;

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
    public String handleCreateAlbum(@ModelAttribute CreateAlbumRequest albumRequest, Authentication authentication, Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String userEmail = authentication.getName();
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario logueado no encontrado."));

        try {
            // Intentamos crear el álbum
            albumService.createAlbum(albumRequest, creator.getId());
            // Si todo va bien, redirigimos
            return "redirect:/albums";

        } catch (IllegalArgumentException e) {
            // Si ocurre un error de validación (como nombre repetido)...
            // Añadimos el mensaje de error que viene del servicio
            model.addAttribute("errorMessage", e.getMessage());
            // Añadimos el objeto de la petición para repoblar el formulario
            model.addAttribute("albumRequest", albumRequest);
            // Buscamos de nuevo los grupos del usuario para mostrarlos en la lista
            model.addAttribute("userGroups", groupService.findGroupsByMember(creator.getId()));

            // Devolvemos la misma vista del formulario en lugar de redirigir
            return "create_album";
        }
    }

    @GetMapping("/albums/{albumId}")
    public String showAlbumDetailPage(
            @PathVariable Long albumId,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) { // <-- Añadir RedirectAttributes

        Album currentAlbum = albumService.findById(albumId);
        User currentUser = null;

        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = userRepository.findByEmail(authentication.getName())
                    .orElse(null);
        }

        // --- VALIDACIÓN DE PERMISOS ---
        if (!albumService.canUserViewAlbum(currentAlbum, currentUser)) {
            // Si no tiene permiso, lo redirigimos con un mensaje de error
            redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para ver este álbum.");
            return "redirect:/albums"; // O a /home
        }

        // Si tiene permiso, continuamos como antes
        model.addAttribute("album", currentAlbum);
        model.addAttribute("ownerAlbums", albumService.findAlbumsByOwnerId(currentAlbum.getOwner().getId()));
        model.addAttribute("activePage", "albums");

        return "album_detail";
    }

    /**
     * AÑADIDO: Procesa el envío del formulario de subida de fotos.
     */
    @PostMapping("/albums/{albumId}/upload")
    public String handleUploadPhoto(
            @PathVariable Long albumId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description,
            Authentication authentication,
            RedirectAttributes redirectAttributes) { // <-- Añadir RedirectAttributes

        try {
            String userEmail = authentication.getName();
            User uploader = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

            photoService.uploadPhoto(file, description, albumId, uploader.getId());

        } catch (IllegalArgumentException | IOException e) {
            // Si ocurre un error, añadimos el mensaje para mostrarlo en el frontend
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            // Y redirigimos de vuelta a la página de subida
            return "redirect:/albums/" + albumId + "/upload";
        }

        // Si todo va bien, redirigimos a la página del álbum
        return "redirect:/albums/" + albumId;
    }

    @GetMapping("/albums/{albumId}/upload")
    public String showUploadPhotoForm(
            @PathVariable Long albumId,
            Model model,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        Album currentAlbum = albumService.findById(albumId);
        User currentUser = null;

        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = userRepository.findByEmail(authentication.getName())
                    .orElse(null);
        }

        // Validamos permisos
        if (!albumService.canUserViewAlbum(currentAlbum, currentUser)) {
            redirectAttributes.addFlashAttribute("errorMessage", "No tienes permiso para subir fotos a este álbum.");
            return "redirect:/albums";
        }

        model.addAttribute("albumId", albumId);
        model.addAttribute("album", currentAlbum);
        return "upload_photo";
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

    @GetMapping("/albums/{albumId}/edit-permissions")
    public String showEditAlbumPermissionsPage(@PathVariable Long albumId, Model model, Authentication authentication) {

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        Album album = albumService.findById(albumId);

        // Validación para que solo el dueño pueda editar
        if (!album.getOwner().equals(currentUser)) {
            return "redirect:/albums";
        }

        model.addAttribute("album", album);
        model.addAttribute("userGroups", groupService.findGroupsByMember(currentUser.getId()));

        return "edit_album_permissions"; // Nombre del nuevo archivo HTML
    }

    // Procesa los cambios de permisos del álbum.
    @PostMapping("/albums/{albumId}/edit-permissions")
    public String handleUpdateAlbumPermissions(
            @PathVariable Long albumId,
            @RequestParam(value = "viewPermissionGroupIds", required = false) List<Long> viewIds,
            @RequestParam(value = "commentPermissionGroupIds", required = false) List<Long> commentIds,
            Authentication authentication) {

        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        albumService.updateAlbumPermissions(albumId, currentUser.getId(), viewIds, commentIds);

        return "redirect:/albums/" + albumId;
    }

}
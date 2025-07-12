package grupo6.umbook.controller;

import grupo6.umbook.dto.CreateAlbumRequest;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.UserRepository;
import grupo6.umbook.service.AlbumService;
import grupo6.umbook.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;

@Controller
public class AlbumPageController {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private GroupService groupService; // Inyectamos el servicio de grupos

    @Autowired
    private UserRepository userRepository;

    /**
     * Muestra la página principal de álbumes del usuario.
     */
    @GetMapping("/albums")
    public String showAlbumsPage(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("Usuario logueado no encontrado."));
            model.addAttribute("albums", albumService.findByOwner(currentUser.getId()));
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
}
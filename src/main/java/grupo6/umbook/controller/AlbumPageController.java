package grupo6.umbook.controller;

import grupo6.umbook.model.User;
import grupo6.umbook.repository.UserRepository;
import grupo6.umbook.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Collections;

@Controller
public class AlbumPageController {

    @Autowired
    private AlbumService albumService;

    // Necesitamos el UserRepository para buscar al usuario logueado
    @Autowired
    private UserRepository userRepository;

    /**
     * Este método maneja las peticiones a "/albums", busca los álbumes
     * del usuario que ha iniciado sesión y muestra la página "albums.html".
     */
    @GetMapping("/albums")
    public String showAlbumsPage(Model model, Authentication authentication) {

        if (authentication != null && authentication.isAuthenticated()) {
            // Obtenemos el email del usuario logueado
            String userEmail = authentication.getName();

            // Buscamos el usuario en la base de datos
            User currentUser = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new IllegalStateException("Usuario logueado no encontrado en la base de datos."));

            // Buscamos los álbumes de ese usuario y los añadimos al modelo
            model.addAttribute("albums", albumService.findByOwner(currentUser.getId()));

        } else {
            // Si por alguna razón no hay nadie logueado, pasamos una lista vacía
            model.addAttribute("albums", Collections.emptyList());
        }

        // Le decimos a Thymeleaf que renderice el archivo "albums.html"
        return "albums";
    }
}
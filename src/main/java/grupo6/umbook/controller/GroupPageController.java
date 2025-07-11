package grupo6.umbook.controller;

import grupo6.umbook.dto.CreateGroupRequest;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.UserRepository;
import grupo6.umbook.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // <-- IMPORT AÑADIDO
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GroupPageController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/groups")
    public String showGroupsPage(Model model) {
        model.addAttribute("groups", groupService.findPublicGroups());
        return "groups";
    }

    @GetMapping("/groups/create")
    public String showCreateGroupPage(Model model) {
        model.addAttribute("groupRequest", new CreateGroupRequest());
        return "create_group";
    }

    /**
     * MODIFICADO: Ahora el método recibe el objeto Authentication
     * para saber quién es el usuario que ha iniciado sesión.
     */
    @PostMapping("/groups/create")
    public String handleCreateGroup(@ModelAttribute CreateGroupRequest groupRequest, Authentication authentication, Model model) {

        // ... (la lógica para obtener el 'creator' se queda igual)
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        String userEmail = authentication.getName();
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));

        try {
            // Intentamos crear el grupo
            groupService.createGroup(groupRequest, creator.getId());

            // Si todo va bien, redirigimos a la lista de grupos
            return "redirect:/groups";

        } catch (IllegalArgumentException e) {
            // Si atrapamos un error...
            if (e.getMessage().contains("Group name already exists")) {
                // Añadimos el mensaje de error y los datos ya escritos al modelo
                model.addAttribute("errorMessage", "The group name cannot be repeated. Please enter a valid name.");
                model.addAttribute("groupRequest", groupRequest); // Esto repuebla el formulario

                // Devolvemos la misma vista del formulario en lugar de redirigir
                return "create_group";
            }

            // Para cualquier otro error, podrías manejarlo de otra forma
            model.addAttribute("errorMessage", "Ocurrió un error inesperado.");
            model.addAttribute("groupRequest", groupRequest);
            return "create_group";
        }
    }
}
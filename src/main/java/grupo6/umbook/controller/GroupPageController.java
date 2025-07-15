package grupo6.umbook.controller;

import grupo6.umbook.dto.CreateGroupRequest;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.UserRepository;
import grupo6.umbook.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // <-- IMPORT AÑADIDO
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class GroupPageController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/groups")
    public String showGroupsPage(Model model, Authentication authentication) {
        model.addAttribute("groups", groupService.findPublicGroups());

        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("currentUserEmail", authentication.getName());
        }

        return "groups";
    }

    @GetMapping("/groups/create")
    public String showCreateGroupPage(Model model, Authentication authentication) {
        model.addAttribute("groupRequest", new CreateGroupRequest());

        // Obtenemos el usuario autenticado
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        // Filtramos al usuario autenticado de la lista
        List<User> otherUsers = userRepository.findAll()
                .stream()
                .filter(user -> !user.getId().equals(currentUser.getId()))
                .toList();

        model.addAttribute("suggestedUsers", otherUsers);

        return "create_group";
    }

    @PostMapping("/groups/create")
    public String handleCreateGroup(@ModelAttribute CreateGroupRequest groupRequest,
                                    @RequestParam(value = "memberIds", required = false) List<Long> memberIds,
                                    Authentication authentication,
                                    Model model) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String userEmail = authentication.getName();
        User creator = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found."));

        try {
            groupService.createGroup(groupRequest, creator.getId(), memberIds);
            return "redirect:/groups";

        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Group name already exists")) {
                model.addAttribute("errorMessage", "The group name cannot be repeated. Please enter a valid name.");
                model.addAttribute("groupRequest", groupRequest);
                return "create_group";
            }

            model.addAttribute("errorMessage", "Ocurrió un error inesperado.");
            model.addAttribute("groupRequest", groupRequest);
            return "create_group";
        }
    }

    @PostMapping("/groups/delete/{groupId}")
    public String deleteGroupWeb(@PathVariable Long groupId,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        try {
            groupService.deleteGroup(groupId, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Grupo eliminado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "No se pudo eliminar el grupo: " + e.getMessage());
        }

        return "redirect:/groups";
    }


}
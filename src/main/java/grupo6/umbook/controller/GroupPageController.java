package grupo6.umbook.controller;

import grupo6.umbook.dto.CreateGroupRequest;
import grupo6.umbook.model.Group;
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

        // Esto ya lo tenías, y está bien
        model.addAttribute("groups", groupService.findPublicGroups());

        // AÑADIDO: Obtenemos el email del usuario actual y lo pasamos a la vista
        if (authentication != null && authentication.isAuthenticated()) {
            // authentication.getName() devuelve el username, que en nuestro caso es el email
            model.addAttribute("currentUserEmail", authentication.getName());
        } else {
            // En caso de que no haya nadie logueado, pasamos un valor nulo
            model.addAttribute("currentUserEmail", null);
        }
        return "groups";
    }

    @GetMapping("/groups/create")
    public String showCreateGroupPage(Model model, Authentication authentication) {
        CreateGroupRequest groupRequest = new CreateGroupRequest();

        // Establecer valores vacíos para que se muestre "Elegir..." en el select
        groupRequest.setPostPermission("");
        groupRequest.setCommentPermission("");
        groupRequest.setInvitePermission("");

        model.addAttribute("groupRequest", groupRequest);

        // Obtenemos el usuario autenticado
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

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
            String msg = e.getMessage();

            if (msg.contains("Group name already exists")) {
                model.addAttribute("errorMessage", "The group name cannot be repeated. Please enter a valid name.");
            } else if (msg.contains("permisos") || msg.contains("permissions")) {
                model.addAttribute("errorMessage", "Debes seleccionar todos los permisos del grupo.");
            } else {
                model.addAttribute("errorMessage", "Ocurrió un error inesperado.");
            }

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

    /**
     * AÑADIDO: Muestra la página de detalle de un grupo específico.
     */
    @GetMapping("/groups/{groupId}")
    public String showGroupDetailPage(@PathVariable Long groupId, Model model, Authentication authentication) {

        Group currentGroup = groupService.findById(groupId);
        model.addAttribute("group", currentGroup);
        model.addAttribute("activePage", "groups");

        // AÑADIDO: Pasamos el email del usuario actual a la vista
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("currentUserEmail", authentication.getName());
        }

        return "group_detail";
    }

    @GetMapping("/groups/{groupId}/add-members")
    public String showAddMembersPage(@PathVariable Long groupId, Model model, Authentication authentication) {

        // Obtenemos el grupo y el usuario actual
        Group group = groupService.findById(groupId);
        String userEmail = authentication.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // Filtramos la lista de amigos para mostrar solo los que no son miembros
        List<User> friendsToAdd = currentUser.getFriends().stream()
                .filter(friend -> !group.getMembers().contains(friend))
                .toList();

        model.addAttribute("group", group);
        model.addAttribute("friendsToAdd", friendsToAdd);

        return "add_members_to_group";
    }

    @PostMapping("/groups/{groupId}/add-members")
    public String handleAddMembers(@PathVariable Long groupId,
                                   @RequestParam(value="memberIds", required = false) List<Long> memberIds,
                                   RedirectAttributes redirectAttributes) {

        // Validamos que se haya seleccionado al menos un amigo
        if (memberIds == null || memberIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Debe seleccionar al menos un amigo para agregar al grupo.");
            return "redirect:/groups/" + groupId + "/add-members";
        }

        groupService.addMembersToGroup(groupId, memberIds);
        return "redirect:/groups/" + groupId;
    }

    @GetMapping("/groups/{groupId}/members")
    public String showGroupMembersPage(@PathVariable Long groupId, Model model) {

        Group currentGroup = groupService.findById(groupId);

        model.addAttribute("group", currentGroup);
        model.addAttribute("activePage", "groups");

        // Renderiza el nuevo archivo "group-members.html"
        return "group_members";
    }
}
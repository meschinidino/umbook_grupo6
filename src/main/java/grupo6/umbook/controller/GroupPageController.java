package grupo6.umbook.controller;

import grupo6.umbook.service.GroupService;
import grupo6.umbook.dto.CreateGroupRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class GroupPageController {

    @Autowired
    private GroupService groupService;

    /**
     * Este método sí maneja la petición GET a "/groups"
     * y devuelve el nombre de la plantilla "groups.html".
     */
    @GetMapping("/groups")
    public String showGroupsPage(Model model) {
        // Obtenemos la lista de grupos desde el servicio
        // Usamos findPublicGroups() como ejemplo, podés cambiarlo por el que necesites
        model.addAttribute("groups", groupService.findPublicGroups());

        // Esto le dice a Thymeleaf que renderice el archivo "groups.html"
        return "groups";
    }

    /**
     * AÑADIDO: Método para mostrar el formulario de creación de grupo.
     */
    @GetMapping("/groups/create")
    public String showCreateGroupPage(Model model) {
        // Pasamos un objeto vacío para que el formulario se enlace a él
        model.addAttribute("groupRequest", new CreateGroupRequest());
        return "create_group"; // Renderiza el nuevo archivo create-group.html
    }

    /**
     * AÑADIDO: Método para procesar el envío del formulario.
     */
    @PostMapping("/groups/create")
    public String handleCreateGroup(@ModelAttribute CreateGroupRequest groupRequest) {
        // Aquí necesitarás obtener el ID del usuario autenticado.
        // Por ahora, usaremos un ID de ejemplo (ej: 1L).
        Long currentUserId = 1L;

        groupService.createGroup(
                groupRequest.getName(),
                groupRequest.getDescription(),
                currentUserId
        );

        // Redirigimos al usuario a la lista de grupos después de crear uno
        return "redirect:/groups";
    }
}
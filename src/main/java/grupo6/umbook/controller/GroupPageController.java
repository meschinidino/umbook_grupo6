package grupo6.umbook.controller;

import grupo6.umbook.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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
}
package grupo6.umbook.controller;

import grupo6.umbook.dto.CreateGroupRequest;
import grupo6.umbook.model.*;
import grupo6.umbook.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");

            if (request.get("creatorId") == null || name == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Name and creator ID are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            Long creatorId = Long.valueOf(request.get("creatorId").toString());

            // 1. Creamos el objeto DTO que el servicio espera
            CreateGroupRequest groupRequest = new CreateGroupRequest();
            groupRequest.setName(name);
            groupRequest.setDescription(description);
            // Aquí también podrías obtener los permisos del 'request' y ponerlos en el DTO
            // groupRequest.setVisibility((String) request.get("visibility"));

            // 2. Llamamos al servicio con la firma correcta (DTO y Long)
            Group group = groupService.createGroup(groupRequest, creatorId);

            return ResponseEntity.status(HttpStatus.CREATED).body(group);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroupById(@PathVariable Long groupId) {
        try {
            Group group = groupService.findById(groupId);
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<Group>> getGroupsByCreator(@PathVariable Long creatorId) {
        List<Group> groups = groupService.findByCreator(creatorId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/member/{userId}")
    public ResponseEntity<List<Group>> getGroupsByMember(@PathVariable Long userId) {
        List<Group> groups = groupService.findGroupsByMember(userId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Group>> getPublicGroups() {
        List<Group> groups = groupService.findPublicGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Group>> searchGroups(@RequestParam String term) {
        List<Group> groups = groupService.searchGroups(term);
        return ResponseEntity.ok(groups);
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Long userId = Long.valueOf(request.get("userId").toString());

            Group group = groupService.updateGroup(groupId, name, description, userId);
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<?> addMemberToGroup(
            @PathVariable Long groupId,
            @RequestBody Map<String, Long> request) {
        try {
            Long userId = request.get("userId");
            Long inviterId = request.get("inviterId");

            if (userId == null || inviterId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "User ID and inviter ID are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Group group = groupService.addMemberToGroup(groupId, userId, inviterId);
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<?> removeMemberFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            @RequestParam Long removerId) {
        try {
            Group group = groupService.removeMemberFromGroup(groupId, userId, removerId);
            return ResponseEntity.ok(group);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // AÑADIDO: Endpoint para el borrado lógico de un grupo.
    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId, @RequestParam Long userId) {
        try {
            groupService.deleteGroup(groupId, userId);
            return ResponseEntity.ok().body(Map.of("message", "Group marked as deleted successfully."));
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{groupId}/permissions")
    public ResponseEntity<?> setGroupPermissions(
            @PathVariable Long groupId,
            @RequestBody Map<String, Object> request) {
        try {
            // --- VALIDACIÓN DE userId ---
            // 1. Verificamos que el userId exista en la petición antes de usarlo.
            Object userIdObj = request.get("userId");
            if (userIdObj == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "userId is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            Long userId = Long.valueOf(userIdObj.toString());

            // --- MANEJO SEGURO DE ENUMS ---
            // 2. Hacemos la conversión a enum insensible a mayúsculas y a prueba de nulos.
            String postPermissionStr = (String) request.get("postPermission");
            String commentPermissionStr = (String) request.get("commentPermission");
            String invitePermissionStr = (String) request.get("invitePermission");

            // Usamos .toUpperCase() para evitar errores por mayúsculas/minúsculas
            GroupPermission postPermission = postPermissionStr != null ?
                    GroupPermission.valueOf(postPermissionStr.toUpperCase()) : null;
            GroupPermission commentPermission = commentPermissionStr != null ?
                    GroupPermission.valueOf(commentPermissionStr.toUpperCase()) : null;
            GroupPermission invitePermission = invitePermissionStr != null ?
                    GroupPermission.valueOf(invitePermissionStr.toUpperCase()) : null;

            Group group = groupService.setGroupPermissions(
                    groupId, userId, postPermission, commentPermission, invitePermission);

            return ResponseEntity.ok(group);

        } catch (IllegalArgumentException e) {
            // Este catch ahora manejará errores si se envía un valor de enum inválido (ej. "PUBLI")
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid value for permission or visibility: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            // Catch general para otros posibles errores
            Map<String, String> response = new HashMap<>();
            response.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<Set<User>> getGroupMembers(@PathVariable Long groupId) {
        try {
            Set<User> members = groupService.getGroupMembers(groupId);
            // Remove passwords from the response
            members.forEach(member -> member.setPassword(null));
            return ResponseEntity.ok(members);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{groupId}/is-member/{userId}")
    public ResponseEntity<Map<String, Boolean>> isUserMemberOfGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        try {
            boolean isMember = groupService.isUserMemberOfGroup(groupId, userId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("isMember", isMember);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
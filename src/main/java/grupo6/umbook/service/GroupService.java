package grupo6.umbook.service;

import grupo6.umbook.dto.CreateGroupRequest;
import grupo6.umbook.model.*;
import grupo6.umbook.repository.GroupRepository;
import grupo6.umbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public GroupService(
            GroupRepository groupRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Group createGroup(CreateGroupRequest request, Long creatorId) {
        String name = request.getName();
        String description = request.getDescription();

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }
        if (groupRepository.existsByName(name)) {
            throw new IllegalArgumentException("Group name already exists");
        }
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        Group group = new Group(name, description, creator);

        // Aquí también establecemos los permisos que vienen en el DTO
        if (request.getVisibility() != null) {
            group.setVisibility(GroupVisibility.valueOf(request.getVisibility().toUpperCase()));
        }
        if (request.getPostPermission() != null) {
            group.setPostPermission(GroupPermission.valueOf(request.getPostPermission().toUpperCase()));
        }

        if (group.getMembers().isEmpty()) {
            throw new IllegalArgumentException("Group must have at least one member.");
        }

        return groupRepository.save(group);
    }

    // NUEVO método: llamado desde el formulario web
    @Transactional
    public Group createGroup(CreateGroupRequest request, Long creatorId, List<Long> memberIds) {
        Group group = createGroup(request, creatorId); // Reutiliza la lógica del original

        if (memberIds != null) {
            for (Long userId : memberIds) {
                if (!userId.equals(creatorId)) {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
                    group.addMember(user);
                }
            }
            groupRepository.save(group);
        }

        return group;
    }

    @Transactional(readOnly = true)
    public Group findById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }

    @Transactional(readOnly = true)
    public List<Group> findByCreator(Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));
        return groupRepository.findByCreator(creator);
    }

    @Transactional(readOnly = true)
    public List<Group> findGroupsByMember(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return groupRepository.findGroupsByMember(user);
    }

    public List<Group> findPublicGroups() {
        return groupRepository.findPublicGroups()
                .stream()
                .filter(g -> g.getState() != GroupState.ELIMINADO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Group> searchGroups(String searchTerm) {
        return groupRepository.findByNameOrDescriptionContaining(searchTerm);
    }

    @Transactional
    public Group updateGroup(Long groupId, String name, String description, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (!group.getCreator().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the creator can update the group");
        }

        if (name != null && !name.trim().isEmpty()) {
            if (!name.equals(group.getName()) && groupRepository.existsByName(name)) {
                throw new IllegalArgumentException("Group name already exists");
            }
            group.setName(name);
        }

        if (description != null) {
            group.setDescription(description);
        }

        return groupRepository.save(group);
    }

    @Transactional
    public Group addMemberToGroup(Long groupId, Long userId, Long inviterId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new IllegalArgumentException("Inviter not found"));

        if (!group.getCreator().equals(inviter) &&
                group.getInvitePermission() == GroupPermission.ADMIN_ONLY) {
            throw new IllegalArgumentException("You don't have permission to add members to this group");
        }

        if (group.getInvitePermission() == GroupPermission.MEMBERS_ONLY &&
                !group.getMembers().contains(inviter)) {
            throw new IllegalArgumentException("You must be a member to add others to this group");
        }

        if (group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        group.addMember(user);
        Group savedGroup = groupRepository.save(group);

        notificationService.createNotification(
                user.getId(),
                inviter.getFirstName() + " " + inviter.getLastName() + " added you to the group " + group.getName(),
                Notification.NotificationType.GROUP_INVITATION,
                savedGroup.getId()
        );

        return savedGroup;
    }

    /**
     * MODIFICADO: Se añade la lógica del diagrama de estados.
     * Si al eliminar un miembro el grupo queda vacío, su estado cambia a SIN_MIEMBROS.
     */
    @Transactional
    public Group removeMemberFromGroup(Long groupId, Long userId, Long removerId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User remover = userRepository.findById(removerId)
                .orElseThrow(() -> new IllegalArgumentException("Remover not found"));

        if (!group.getCreator().equals(remover) && !user.equals(remover)) {
            throw new IllegalArgumentException("You don't have permission to remove this member");
        }

        if (!group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is not a member of this group");
        }

        if (user.equals(group.getCreator())) {
            throw new IllegalArgumentException("Cannot remove the creator from the group");
        }

        group.removeMember(user);

        // Lógica del diagrama de estados
        if (group.getMembers().isEmpty()) {
            group.setState(GroupState.SIN_MIEMBROS);
        }

        return groupRepository.save(group);
    }

    /**
     * AÑADIDO: Método para el borrado lógico según el diagrama de estados.
     * Solo se puede eliminar un grupo si su estado es SIN_MIEMBROS.
     */
    @Transactional
    public void deleteGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Solo el creador puede eliminar el grupo
        if (!group.getCreator().equals(user)) {
            throw new IllegalArgumentException("Only the creator can delete the group");
        }

        // Permitimos borrar el grupo incluso con miembros
        group.setMembers(new HashSet<>()); // Limpiamos la lista de miembros
        group.setState(GroupState.ELIMINADO);
        groupRepository.save(group);
    }

    @Transactional
    public Group setGroupPermissions(Long groupId, Long userId,
                                     GroupVisibility visibility,
                                     GroupPermission postPermission,
                                     GroupPermission commentPermission,
                                     GroupPermission invitePermission) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!group.getCreator().equals(user)) {
            throw new IllegalArgumentException("Only the creator can set group permissions");
        }

        if (visibility != null) {
            group.setVisibility(visibility);
        }
        if (postPermission != null) {
            group.setPostPermission(postPermission);
        }
        if (commentPermission != null) {
            group.setCommentPermission(commentPermission);
        }
        if (invitePermission != null) {
            group.setInvitePermission(invitePermission);
        }

        return groupRepository.save(group);
    }

    @Transactional(readOnly = true)
    public Set<User> getGroupMembers(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        return group.getMembers();
    }

    @Transactional(readOnly = true)
    public boolean isUserMemberOfGroup(Long groupId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return groupRepository.isUserMemberOfGroup(groupId, user);
    }
}
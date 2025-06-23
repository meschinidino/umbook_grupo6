package grupo6.umbook.service;

import grupo6.umbook.model.Group;
import grupo6.umbook.model.Notification;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.GroupRepository;
import grupo6.umbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Group createGroup(String name, String description, Long creatorId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }

        if (groupRepository.existsByName(name)) {
            throw new IllegalArgumentException("Group name already exists");
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        Group group = new Group(name, description, creator);
        return groupRepository.save(group);
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

    @Transactional(readOnly = true)
    public List<Group> findPublicGroups() {
        return groupRepository.findPublicGroups();
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
            // Check if the new name is different from the current one and not already taken
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

        // Check if the inviter has permission to add members
        if (!group.getCreator().equals(inviter) && 
            group.getInvitePermission() == Group.GroupPermission.ADMIN_ONLY) {
            throw new IllegalArgumentException("You don't have permission to add members to this group");
        }

        // Check if the inviter is a member (for MEMBERS_ONLY permission)
        if (group.getInvitePermission() == Group.GroupPermission.MEMBERS_ONLY && 
            !group.getMembers().contains(inviter)) {
            throw new IllegalArgumentException("You must be a member to add others to this group");
        }

        // Check if the user is already a member
        if (group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        group.addMember(user);
        Group savedGroup = groupRepository.save(group);

        // Create notification for the user using NotificationService
        notificationService.createNotification(
                user.getId(),
                inviter.getFirstName() + " " + inviter.getLastName() + " added you to the group " + group.getName(),
                Notification.NotificationType.GROUP_INVITATION,
                savedGroup.getId()
        );

        return savedGroup;
    }

    @Transactional
    public Group removeMemberFromGroup(Long groupId, Long userId, Long removerId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User remover = userRepository.findById(removerId)
                .orElseThrow(() -> new IllegalArgumentException("Remover not found"));

        // Check if the remover is the creator or the user themselves
        if (!group.getCreator().equals(remover) && !user.equals(remover)) {
            throw new IllegalArgumentException("You don't have permission to remove this member");
        }

        // Check if the user is a member
        if (!group.getMembers().contains(user)) {
            throw new IllegalArgumentException("User is not a member of this group");
        }

        // Cannot remove the creator
        if (user.equals(group.getCreator())) {
            throw new IllegalArgumentException("Cannot remove the creator from the group");
        }

        group.removeMember(user);
        return groupRepository.save(group);
    }

    @Transactional
    public Group setGroupPermissions(Long groupId, Long userId, 
                                    Group.GroupVisibility visibility,
                                    Group.GroupPermission postPermission,
                                    Group.GroupPermission commentPermission,
                                    Group.GroupPermission invitePermission) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if the user is the creator
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
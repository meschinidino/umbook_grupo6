package grupo6.umbook.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToMany
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // AÑADIDO: Campo para el diagrama de estados
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupState state = GroupState.CON_MIEMBROS;

    @Enumerated(EnumType.STRING)
    private GroupVisibility visibility = GroupVisibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    private GroupPermission postPermission = GroupPermission.ALL;

    @Enumerated(EnumType.STRING)
    private GroupPermission commentPermission = GroupPermission.ALL;

    @Enumerated(EnumType.STRING)
    private GroupPermission invitePermission = GroupPermission.ALL;

    // Constructores
    public Group() {
    }

    public Group(String name, String description, User creator) {
        this.name = name;
        this.description = description;
        this.creator = creator;
        this.members.add(creator); // El creador es miembro automáticamente
        this.state = GroupState.CON_MIEMBROS; // Al tener un miembro, el estado es CON_MIEMBROS
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
    public Set<User> getMembers() { return members; }
    public void setMembers(Set<User> members) { this.members = members; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public GroupState getState() { return state; } // AÑADIDO
    public void setState(GroupState state) { this.state = state; } // AÑADIDO
    public GroupVisibility getVisibility() { return visibility; }
    public void setVisibility(GroupVisibility visibility) { this.visibility = visibility; }
    public GroupPermission getPostPermission() { return postPermission; }
    public void setPostPermission(GroupPermission postPermission) { this.postPermission = postPermission; }
    public GroupPermission getCommentPermission() { return commentPermission; }
    public void setCommentPermission(GroupPermission commentPermission) { this.commentPermission = commentPermission; }
    public GroupPermission getInvitePermission() { return invitePermission; }
    public void setInvitePermission(GroupPermission invitePermission) { this.invitePermission = invitePermission; }

    // Métodos de ayuda
    public void addMember(User user) { this.members.add(user); }
    public void removeMember(User user) { this.members.remove(user); }
    public boolean isMember(User user) { return this.members.contains(user); }
    public boolean isCreator(User user) { return this.creator.equals(user); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return id != null && id.equals(group.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
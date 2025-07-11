package grupo6.umbook.dto;

import org.springframework.web.multipart.MultipartFile; // <-- AÑADIR ESTE IMPORT

public class CreateGroupRequest {

    private String name;
    private String description;
    private String visibility;
    private String postPermission;
    private String commentPermission;
    private String invitePermission;

    // MODIFICADO: El tipo de dato ahora es MultipartFile
    private MultipartFile coverPhoto;
    private MultipartFile profilePhoto;

    // --- GETTERS Y SETTERS ---
    // (Los getters y setters para name, description, y los permisos se quedan igual)

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }
    public String getPostPermission() { return postPermission; }
    public void setPostPermission(String postPermission) { this.postPermission = postPermission; }
    public String getCommentPermission() { return commentPermission; }
    public void setCommentPermission(String commentPermission) { this.commentPermission = commentPermission; }
    public String getInvitePermission() { return invitePermission; }
    public void setInvitePermission(String invitePermission) { this.invitePermission = invitePermission; }

    // MODIFICADOS: Getters y Setters para los archivos
    public MultipartFile getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(MultipartFile coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public MultipartFile getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(MultipartFile profilePhoto) {
        this.profilePhoto = profilePhoto;
    }
}
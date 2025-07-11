package grupo6.umbook.dto;

/**
 * Esta clase (DTO) se usa para transportar los datos del formulario
 * para crear un nuevo grupo.
 */
public class CreateGroupRequest {

    private String name;
    private String description;

    // Dejamos los campos para las fotos como String por simplicidad,
    // en una implementación real manejarías archivos (MultipartFile).
    private String coverPhoto;
    private String profilePhoto;

    // Getters y Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }
}
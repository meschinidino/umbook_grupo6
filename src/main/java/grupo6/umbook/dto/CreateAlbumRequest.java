package grupo6.umbook.dto;

import java.util.List;

public class CreateAlbumRequest {
    private String name;
    private String description;
    private List<Long> viewPermissionGroupIds;
    private List<Long> commentPermissionGroupIds;

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

    public List<Long> getViewPermissionGroupIds() {
        return viewPermissionGroupIds;
    }

    public void setViewPermissionGroupIds(List<Long> viewPermissionGroupIds) {
        this.viewPermissionGroupIds = viewPermissionGroupIds;
    }

    public List<Long> getCommentPermissionGroupIds() {
        return commentPermissionGroupIds;
    }

    public void setCommentPermissionGroupIds(List<Long> commentPermissionGroupIds) {
        this.commentPermissionGroupIds = commentPermissionGroupIds;
    }
}
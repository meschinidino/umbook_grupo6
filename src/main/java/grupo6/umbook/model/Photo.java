package grupo6.umbook.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "photos")
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(length = 1000)
    private String description;

    @Lob
    @Column(nullable = false, columnDefinition="LONGBLOB")
    private byte[] data;

    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User uploader;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "photo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    // Constructors
    public Photo() {
    }

    public Photo(String fileName, String contentType, byte[] data, User uploader) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.data = data;
        this.uploader = uploader;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public User getUploader() {
        return uploader;
    }

    public void setUploader(User uploader) {
        this.uploader = uploader;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    // Helper methods
    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPhoto(this);
    }

    public void removeComment(Comment comment) {
        comments.remove(comment);
        comment.setPhoto(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // Usamos instanceof para verificar el tipo, es más seguro con proxies de Hibernate
        if (!(o instanceof Photo)) return false;
        Photo photo = (Photo) o;
        // La igualdad se basa en el ID si no es nulo
        return id != null && id.equals(photo.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
package grupo6.umbook.service;

import grupo6.umbook.model.Album;
import grupo6.umbook.model.AlbumState;
import grupo6.umbook.model.Photo;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.AlbumRepository;
import grupo6.umbook.repository.PhotoRepository;
import grupo6.umbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(
            Arrays.asList("image/jpeg", "image/png", "image/gif"));
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Autowired
    public PhotoService(
            PhotoRepository photoRepository,
            AlbumRepository albumRepository,
            UserRepository userRepository) {
        this.photoRepository = photoRepository;
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Photo uploadPhoto(MultipartFile file, String description, Long albumId, Long uploaderId) throws IOException {
        validateFile(file);
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found"));
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new IllegalArgumentException("Uploader not found"));
        if (!album.getOwner().getId().equals(uploaderId)) {
            throw new IllegalArgumentException("Only the album owner can upload photos");
        }
        if (photoRepository.existsByAlbumIdAndFileName(albumId, file.getOriginalFilename())) {
            throw new IllegalArgumentException("PThis photo already exists in this album");
        }

        Photo photo = new Photo(file.getOriginalFilename(), file.getContentType(), file.getBytes(), uploader);
        photo.setDescription(description);

        // La lógica de asociación ya la hace album.addPhoto

        Photo savedPhoto = photoRepository.save(photo);
        album.addPhoto(savedPhoto);

        // AÑADIDO: Lógica del diagrama de estados
        album.setState(AlbumState.CON_FOTO);

        albumRepository.save(album);

        return savedPhoto;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the maximum allowed size of 5MB");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: JPG, PNG, GIF");
        }
    }

    @Transactional(readOnly = true)
    public Photo findById(Long photoId) {
        return photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found"));
    }

    @Transactional(readOnly = true)
    public Photo findByIdAndAlbumId(Long photoId, Long albumId) {
        return photoRepository.findByIdAndAlbumId(photoId, albumId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found in the specified album"));
    }

    @Transactional(readOnly = true)
    public List<Photo> findByAlbum(Long albumId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found"));
        return photoRepository.findByAlbum(album);
    }

    @Transactional(readOnly = true)
    public List<Photo> findByAlbumOrderByUploadedAtDesc(Long albumId) {
        return photoRepository.findByAlbumIdOrderByUploadedAtDesc(albumId);
    }

    @Transactional(readOnly = true)
    public List<Photo> findByUploader(Long uploaderId) {
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new IllegalArgumentException("Uploader not found"));
        return photoRepository.findByUploader(uploader);
    }

    @Transactional(readOnly = true)
    public List<Photo> findByAlbumOwner(Long ownerId) {
        return photoRepository.findByAlbumOwnerIdOrderByUploadedAtDesc(ownerId);
    }

    @Transactional
    public Photo updatePhotoDescription(Long photoId, String description, Long uploaderId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found"));

        // Check if uploader is the photo uploader or album owner
        if (!photo.getUploader().getId().equals(uploaderId) &&
            !photo.getAlbum().getOwner().getId().equals(uploaderId)) {
            throw new IllegalArgumentException("Only the uploader or album owner can update the photo");
        }

        photo.setDescription(description);
        return photoRepository.save(photo);
    }

    @Transactional
    public void deletePhoto(Long photoId, Long userId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found"));

        if (!photo.getUploader().getId().equals(userId) &&
                !photo.getAlbum().getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Only the uploader or album owner can delete the photo");
        }

        Album album = photo.getAlbum();
        album.removePhoto(photo);

        if (album.getPhotos().isEmpty()) {
            album.setState(AlbumState.VACIO);
        }

        albumRepository.save(album); // Guardamos el álbum con su nuevo estado
        photoRepository.delete(photo); // Eliminamos la foto físicamente
    }

    @Transactional(readOnly = true)
    public long countCommentsByPhotoId(Long photoId) {
        // Check if photo exists
        if (!photoRepository.existsById(photoId)) {
            throw new IllegalArgumentException("Photo not found");
        }
        return photoRepository.countCommentsByPhotoId(photoId);
    }

    @Transactional(readOnly = true)
    public boolean isUploaderOrAlbumOwner(Long photoId, Long userId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found"));
        return photo.getUploader().getId().equals(userId) ||
               photo.getAlbum().getOwner().getId().equals(userId);
    }
}
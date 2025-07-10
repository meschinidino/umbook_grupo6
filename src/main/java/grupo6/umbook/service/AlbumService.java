package grupo6.umbook.service;

import grupo6.umbook.model.Album;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.AlbumRepository;
import grupo6.umbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    @Autowired
    public AlbumService(AlbumRepository albumRepository, UserRepository userRepository) {
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Album createAlbum(String name, String description, Long ownerId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Album name cannot be empty");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        // Check if album with same name already exists for this user
        if (albumRepository.existsByNameAndOwner(name, owner)) {
            throw new IllegalArgumentException("Album with this name already exists for this user");
        }

        Album album = new Album(name, description, owner);
        return albumRepository.save(album);
    }

    @Transactional(readOnly = true)
    public Album findById(Long albumId) {
        return albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found"));
    }

    @Transactional(readOnly = true)
    public List<Album> findByOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        return albumRepository.findByOwner(owner);
    }

    @Transactional(readOnly = true)
    public List<Album> findByOwnerOrderByCreatedAtDesc(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        return albumRepository.findByOwnerOrderByCreatedAtDesc(owner);
    }

    @Transactional(readOnly = true)
    public Optional<Album> findByNameAndOwner(String name, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
        return albumRepository.findByNameAndOwner(name, owner);
    }

    @Transactional
    public Album updateAlbum(Long albumId, String name, String description, Long ownerId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found"));

        // Check if the user is the owner
        if (!album.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the owner can update the album");
        }

        if (name != null && !name.trim().isEmpty()) {
            // Check if the new name is different from the current one and not already taken
            if (!name.equals(album.getName()) && 
                albumRepository.existsByNameAndOwner(name, album.getOwner())) {
                throw new IllegalArgumentException("Album with this name already exists for this user");
            }
            album.setName(name);
        }

        if (description != null) {
            album.setDescription(description);
        }

        album.setUpdatedAt(LocalDateTime.now());
        return albumRepository.save(album);
    }

    @Transactional
    public void deleteAlbum(Long albumId, Long ownerId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found"));

        // Check if the user is the owner
        if (!album.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the owner can delete the album");
        }

        albumRepository.delete(album);
    }

    @Transactional(readOnly = true)
    public long countPhotosByAlbumId(Long albumId) {
        // Check if album exists
        if (!albumRepository.existsById(albumId)) {
            throw new IllegalArgumentException("Album not found");
        }
        return albumRepository.countPhotosByAlbumId(albumId);
    }

    @Transactional(readOnly = true)
    public List<Album> searchAlbums(String searchTerm) {
        return albumRepository.findByNameOrDescriptionContaining(searchTerm);
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long albumId, Long userId) {
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found"));
        return album.getOwner().getId().equals(userId);
    }
}
package grupo6.umbook.service;

import grupo6.umbook.dto.CreateAlbumRequest;
import grupo6.umbook.model.*;
import grupo6.umbook.repository.AlbumRepository;
import grupo6.umbook.repository.GroupRepository;
import grupo6.umbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    public AlbumService(AlbumRepository albumRepository, UserRepository userRepository, GroupRepository groupRepository) {
        this.albumRepository = albumRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional
    public Album createAlbum(CreateAlbumRequest request, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Owner not found"));

        String name = request.getName();
        String description = request.getDescription();

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Album name cannot be empty.");
        }
        if (albumRepository.existsByNameAndOwner(name, owner)) {
            throw new IllegalArgumentException("You already have an album with that name.");
        }

        Album album = new Album(name, description, owner);

        if (request.getViewPermissionGroupIds() != null) {
            List<Group> viewGroups = groupRepository.findAllById(request.getViewPermissionGroupIds());
            album.setPermittedToView(new HashSet<>(viewGroups));
        }
        if (request.getCommentPermissionGroupIds() != null) {
            List<Group> commentGroups = groupRepository.findAllById(request.getCommentPermissionGroupIds());
            album.setPermittedToComment(new HashSet<>(commentGroups));
        }

        return albumRepository.save(album);
    }

    @Transactional(readOnly = true)
    public Album findById(Long albumId) {
        // MODIFICADO: Usamos el nuevo método para asegurar que todo se cargue.
        return albumRepository.findByIdWithDetails(albumId)
                .orElseThrow(() -> new IllegalArgumentException("Album not found"));
    }

    @Transactional(readOnly = true)
    public List<Album> findAlbumsByOwnerId(Long ownerId) {
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

        // Validamos que el que borra sea el dueño
        if (!album.getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Only the owner can delete the album");
        }

        // MODIFICADO: En lugar de borrar, cambiamos el estado
        album.setState(AlbumState.ELIMINADO);
        albumRepository.save(album);
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

    /**
     * AÑADIDO: Verifica si un usuario puede ver un álbum.
     */
    @Transactional(readOnly = true)
    public boolean canUserViewAlbum(Album album, User user) {

        // Si no hay un usuario logueado, no puede ver el álbum (asumimos que todos son privados por defecto).
        if (user == null) {
            return false;
        }

        // Opción 1: El usuario es el dueño del álbum.
        if (album.getOwner().equals(user)) {
            return true;
        }

        // Opción 2: El usuario es miembro de un grupo con permiso para ver.
        // Collections.disjoint devuelve 'false' si hay al menos un elemento en común.
        return !Collections.disjoint(user.getGroups(), album.getPermittedToView());
    }
}
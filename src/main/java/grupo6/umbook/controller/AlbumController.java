package grupo6.umbook.controller;

import grupo6.umbook.model.Album;
import grupo6.umbook.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final AlbumService albumService;

    @Autowired
    public AlbumController(AlbumService albumService) {
        this.albumService = albumService;
    }

    @PostMapping
    public ResponseEntity<?> createAlbum(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Long ownerId = Long.valueOf(request.get("ownerId").toString());

            if (name == null || ownerId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Name and owner ID are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Album album = albumService.createAlbum(name, description, ownerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(album);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<?> getAlbumById(@PathVariable Long albumId) {
        try {
            Album album = albumService.findById(albumId);
            return ResponseEntity.ok(album);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Album>> getAlbumsByOwner(@PathVariable Long ownerId) {
        List<Album> albums = albumService.findByOwner(ownerId);
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/owner/{ownerId}/recent")
    public ResponseEntity<List<Album>> getRecentAlbumsByOwner(@PathVariable Long ownerId) {
        List<Album> albums = albumService.findByOwnerOrderByCreatedAtDesc(ownerId);
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Album>> searchAlbums(@RequestParam String term) {
        List<Album> albums = albumService.searchAlbums(term);
        return ResponseEntity.ok(albums);
    }

    @GetMapping("/name")
    public ResponseEntity<?> getAlbumByNameAndOwner(
            @RequestParam String name,
            @RequestParam Long ownerId) {
        Optional<Album> albumOpt = albumService.findByNameAndOwner(name, ownerId);
        if (albumOpt.isPresent()) {
            return ResponseEntity.ok(albumOpt.get());
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Album not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<?> updateAlbum(
            @PathVariable Long albumId,
            @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Long ownerId = Long.valueOf(request.get("ownerId").toString());

            Album album = albumService.updateAlbum(albumId, name, description, ownerId);
            return ResponseEntity.ok(album);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<?> deleteAlbum(
            @PathVariable Long albumId,
            @RequestParam Long ownerId) {
        try {
            albumService.deleteAlbum(albumId, ownerId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Album deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{albumId}/photos/count")
    public ResponseEntity<?> countPhotosByAlbumId(@PathVariable Long albumId) {
        try {
            long count = albumService.countPhotosByAlbumId(albumId);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{albumId}/is-owner/{userId}")
    public ResponseEntity<Map<String, Boolean>> isOwner(
            @PathVariable Long albumId,
            @PathVariable Long userId) {
        try {
            boolean isOwner = albumService.isOwner(albumId, userId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("isOwner", isOwner);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
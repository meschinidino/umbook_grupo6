package grupo6.umbook.controller;

import grupo6.umbook.model.Photo;
import grupo6.umbook.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    @Autowired
    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("albumId") Long albumId,
            @RequestParam("uploaderId") Long uploaderId) {
        try {
            Photo photo = photoService.uploadPhoto(file, description, albumId, uploaderId);
            return ResponseEntity.status(HttpStatus.CREATED).body(photo);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload photo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<?> getPhotoById(@PathVariable Long photoId) {
        try {
            Photo photo = photoService.findById(photoId);
            return ResponseEntity.ok(photo);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/{photoId}/data")
    public ResponseEntity<?> getPhotoData(@PathVariable Long photoId) {
        try {
            Photo photo = photoService.findById(photoId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(photo.getContentType()));
            headers.setContentDispositionFormData("attachment", photo.getFileName());
            
            return new ResponseEntity<>(photo.getData(), headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/album/{albumId}")
    public ResponseEntity<List<Photo>> getPhotosByAlbum(@PathVariable Long albumId) {
        List<Photo> photos = photoService.findByAlbum(albumId);
        return ResponseEntity.ok(photos);
    }

    @GetMapping("/album/{albumId}/recent")
    public ResponseEntity<List<Photo>> getRecentPhotosByAlbum(@PathVariable Long albumId) {
        List<Photo> photos = photoService.findByAlbumOrderByUploadedAtDesc(albumId);
        return ResponseEntity.ok(photos);
    }

    @GetMapping("/uploader/{uploaderId}")
    public ResponseEntity<List<Photo>> getPhotosByUploader(@PathVariable Long uploaderId) {
        List<Photo> photos = photoService.findByUploader(uploaderId);
        return ResponseEntity.ok(photos);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Photo>> getPhotosByAlbumOwner(@PathVariable Long ownerId) {
        List<Photo> photos = photoService.findByAlbumOwner(ownerId);
        return ResponseEntity.ok(photos);
    }

    @PutMapping("/{photoId}/description")
    public ResponseEntity<?> updatePhotoDescription(
            @PathVariable Long photoId,
            @RequestBody Map<String, Object> request) {
        try {
            String description = (String) request.get("description");
            Long uploaderId = Long.valueOf(request.get("uploaderId").toString());

            Photo photo = photoService.updatePhotoDescription(photoId, description, uploaderId);
            return ResponseEntity.ok(photo);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> deletePhoto(
            @PathVariable Long photoId,
            @RequestParam Long userId) {
        try {
            photoService.deletePhoto(photoId, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Photo deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{photoId}/comments/count")
    public ResponseEntity<?> countCommentsByPhotoId(@PathVariable Long photoId) {
        try {
            long count = photoService.countCommentsByPhotoId(photoId);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{photoId}/is-owner/{userId}")
    public ResponseEntity<Map<String, Boolean>> isUploaderOrAlbumOwner(
            @PathVariable Long photoId,
            @PathVariable Long userId) {
        try {
            boolean isOwner = photoService.isUploaderOrAlbumOwner(photoId, userId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("isOwner", isOwner);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
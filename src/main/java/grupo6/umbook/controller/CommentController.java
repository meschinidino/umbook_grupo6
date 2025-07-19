package grupo6.umbook.controller;

import grupo6.umbook.model.Comment;
import grupo6.umbook.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/photo")
    public ResponseEntity<?> addCommentToPhoto(@RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            Long photoId = Long.valueOf(request.get("photoId").toString());
            Long authorId = Long.valueOf(request.get("authorId").toString());

            if (content == null || photoId == null || authorId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Content, photo ID, and author ID are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Comment comment = commentService.addCommentToPhoto(content, photoId, authorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/post")
    public ResponseEntity<?> addCommentToPost(@RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            Long postId = Long.valueOf(request.get("postId").toString());
            Long authorId = Long.valueOf(request.get("authorId").toString());

            if (content == null || postId == null || authorId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Content, post ID, and author ID are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Comment comment = commentService.addCommentToPost(content, postId, authorId);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<?> getCommentById(@PathVariable Long commentId) {
        try {
            Comment comment = commentService.findById(commentId);
            return ResponseEntity.ok(comment);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/photo/{photoId}")
    public ResponseEntity<List<Comment>> getCommentsByPhoto(@PathVariable Long photoId) {
        List<Comment> comments = commentService.findByPhoto(photoId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        List<Comment> comments = commentService.findByPost(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Comment>> getCommentsByAuthor(@PathVariable Long authorId) {
        List<Comment> comments = commentService.findByAuthor(authorId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/user-photos/{userId}")
    public ResponseEntity<List<Comment>> getCommentsOnUserPhotos(@PathVariable Long userId) {
        List<Comment> comments = commentService.findCommentsOnUserPhotos(userId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/user-wall/{userId}")
    public ResponseEntity<List<Comment>> getCommentsOnUserWall(@PathVariable Long userId) {
        List<Comment> comments = commentService.findCommentsOnUserWall(userId);
        return ResponseEntity.ok(comments);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            Long authorId = Long.valueOf(request.get("authorId").toString());

            if (content == null || authorId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Content and author ID are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Comment comment = commentService.updateComment(commentId, content, authorId);
            return ResponseEntity.ok(comment);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        try {
            commentService.deleteComment(commentId, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Comment deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{commentId}/mark-deleted")
    public ResponseEntity<?> markCommentAsDeleted(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> request) {
        try {
            Long adminId = Long.valueOf(request.get("adminId").toString());
            String reason = (String) request.get("reason");

            if (adminId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Admin ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Comment comment = commentService.markCommentAsDeleted(commentId, adminId, reason);
            return ResponseEntity.ok(comment);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{commentId}/is-owner/{userId}")
    public ResponseEntity<Map<String, Boolean>> isCommentOwner(
            @PathVariable Long commentId,
            @PathVariable Long userId) {
        try {
            boolean isOwner = commentService.isCommentOwner(commentId, userId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("isOwner", isOwner);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
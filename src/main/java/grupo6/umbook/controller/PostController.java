package grupo6.umbook.controller;

import grupo6.umbook.model.Post;
import grupo6.umbook.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/wall")
    public ResponseEntity<?> createWallPost(@RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            Long authorId = Long.valueOf(request.get("authorId").toString());
            Long wallOwnerId = Long.valueOf(request.get("wallOwnerId").toString());

            if (content == null || authorId == null || wallOwnerId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Content, author ID, and wall owner ID are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Post post = postService.createWallPost(content, authorId, wallOwnerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/group")
    public ResponseEntity<?> createGroupPost(@RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            Long authorId = Long.valueOf(request.get("authorId").toString());
            Long groupId = Long.valueOf(request.get("groupId").toString());

            if (content == null || authorId == null || groupId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Content, author ID, and group ID are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Post post = postService.createGroupPost(content, authorId, groupId);
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable Long postId) {
        try {
            Post post = postService.findById(postId);
            return ResponseEntity.ok(post);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Post>> getPostsByAuthor(@PathVariable Long authorId) {
        List<Post> posts = postService.findByAuthor(authorId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/wall/{wallOwnerId}")
    public ResponseEntity<List<Post>> getPostsByWallOwner(@PathVariable Long wallOwnerId) {
        List<Post> posts = postService.findByWallOwner(wallOwnerId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<Post>> getPostsByGroup(@PathVariable Long groupId) {
        List<Post> posts = postService.findByGroup(groupId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/wall/{wallOwnerId}/active")
    public ResponseEntity<List<Post>> getActivePostsByWallOwner(@PathVariable Long wallOwnerId) {
        List<Post> posts = postService.findActivePostsByWallOwner(wallOwnerId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/group/{groupId}/active")
    public ResponseEntity<List<Post>> getActivePostsByGroup(@PathVariable Long groupId) {
        List<Post> posts = postService.findActivePostsByGroup(groupId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/feed/{userId}")
    public ResponseEntity<List<Post>> getFeedForUser(@PathVariable Long userId) {
        List<Post> posts = postService.getFeedForUser(userId);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable Long postId,
            @RequestBody Map<String, Object> request) {
        try {
            String content = (String) request.get("content");
            Long authorId = Long.valueOf(request.get("authorId").toString());

            if (content == null || authorId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Content and author ID are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Post post = postService.updatePost(postId, content, authorId);
            return ResponseEntity.ok(post);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        try {
            postService.deletePost(postId, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Post deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{postId}/mark-deleted")
    public ResponseEntity<?> markPostAsDeleted(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        try {
            Post post = postService.markPostAsDeleted(postId, userId);
            return ResponseEntity.ok(post);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{postId}/comments/count")
    public ResponseEntity<?> countCommentsByPostId(@PathVariable Long postId) {
        try {
            long count = postService.countCommentsByPostId(postId);
            Map<String, Long> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{postId}/is-owner/{userId}")
    public ResponseEntity<Map<String, Boolean>> isPostOwner(
            @PathVariable Long postId,
            @PathVariable Long userId) {
        try {
            boolean isOwner = postService.isPostOwner(postId, userId);
            Map<String, Boolean> response = new HashMap<>();
            response.put("isOwner", isOwner);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
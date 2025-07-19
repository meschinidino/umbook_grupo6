package grupo6.umbook.service;

import grupo6.umbook.model.Comment;
import grupo6.umbook.model.Notification;
import grupo6.umbook.model.Photo;
import grupo6.umbook.model.Post;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.CommentRepository;
import grupo6.umbook.repository.PhotoRepository;
import grupo6.umbook.repository.PostRepository;
import grupo6.umbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PhotoRepository photoRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Autowired
    public CommentService(
            CommentRepository commentRepository,
            UserRepository userRepository,
            PhotoRepository photoRepository,
            PostRepository postRepository,
            NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.photoRepository = photoRepository;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Comment addCommentToPhoto(String content, Long photoId, Long authorId) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new IllegalArgumentException("Photo not found"));

        Comment comment = new Comment(content, author);
        comment.setPhoto(photo);
        Comment savedComment = commentRepository.save(comment);

        // Create notification for the photo owner if different from commenter using NotificationService
        if (!photo.getUploader().getId().equals(authorId)) {
            notificationService.createNotification(
                    photo.getUploader().getId(),
                    author.getFirstName() + " " + author.getLastName() + " commented on your photo",
                    Notification.NotificationType.NEW_COMMENT,
                    savedComment.getId()
            );
        }

        return savedComment;
    }

    @Transactional
    public Comment addCommentToPost(String content, Long postId, Long authorId) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Comment comment = new Comment(content, author);
        comment.setPost(post);
        Comment savedComment = commentRepository.save(comment);

        // Create notification for the post author if different from commenter using NotificationService
        if (!post.getAuthor().getId().equals(authorId)) {
            notificationService.createNotification(
                    post.getAuthor().getId(),
                    author.getFirstName() + " " + author.getLastName() + " commented on your post",
                    Notification.NotificationType.NEW_COMMENT,
                    savedComment.getId()
            );
        }

        // Create notification for the wall owner if different from commenter and post author using NotificationService
        if (post.getWallOwner() != null && 
            !post.getWallOwner().getId().equals(authorId) && 
            !post.getWallOwner().getId().equals(post.getAuthor().getId())) {
            notificationService.createNotification(
                    post.getWallOwner().getId(),
                    author.getFirstName() + " " + author.getLastName() + " commented on a post on your wall",
                    Notification.NotificationType.NEW_COMMENT,
                    savedComment.getId()
            );
        }

        return savedComment;
    }

    @Transactional(readOnly = true)
    public Comment findById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
    }

    @Transactional(readOnly = true)
    public List<Comment> findByPhoto(Long photoId) {
        return commentRepository.findByPhotoIdOrderByCreatedAtDesc(photoId);
    }

    @Transactional(readOnly = true)
    public List<Comment> findByPost(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    @Transactional(readOnly = true)
    public List<Comment> findByAuthor(Long authorId) {
        return commentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    @Transactional(readOnly = true)
    public List<Comment> findCommentsOnUserPhotos(Long userId) {
        return commentRepository.findCommentsOnUserPhotos(userId);
    }

    @Transactional(readOnly = true)
    public List<Comment> findCommentsOnUserWall(Long userId) {
        return commentRepository.findCommentsOnUserWall(userId);
    }

    @Transactional
    public Comment updateComment(Long commentId, String content, Long authorId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        // Check if the user is the author
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new IllegalArgumentException("Only the author can update the comment");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        // Check if the user is the author, photo owner, or post owner/wall owner
        boolean isAuthor = comment.getAuthor().getId().equals(userId);
        boolean isPhotoOwner = comment.getPhoto() != null && 
                              comment.getPhoto().getAlbum().getOwner().getId().equals(userId);
        boolean isPostAuthor = comment.getPost() != null && 
                              comment.getPost().getAuthor().getId().equals(userId);
        boolean isWallOwner = comment.getPost() != null && 
                             comment.getPost().getWallOwner() != null && 
                             comment.getPost().getWallOwner().getId().equals(userId);

        if (!(isAuthor || isPhotoOwner || isPostAuthor || isWallOwner)) {
            throw new IllegalArgumentException("You don't have permission to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Transactional
    public Comment markCommentAsDeleted(Long commentId, Long adminId, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        // In a real application, you would check if the user is an admin
        // For simplicity, we'll just assume the user with adminId is an admin

        comment.markAsDeleted(reason);
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public boolean isCommentOwner(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
        return comment.getAuthor().getId().equals(userId);
    }
}
package grupo6.umbook.service;

import grupo6.umbook.model.*;
import grupo6.umbook.repository.GroupRepository;
import grupo6.umbook.repository.PostRepository;
import grupo6.umbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final NotificationService notificationService;

    private static final int MAX_POST_LENGTH = 500;

    @Autowired
    public PostService(
            PostRepository postRepository,
            UserRepository userRepository,
            GroupRepository groupRepository,
            NotificationService notificationService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Post createWallPost(String content, Long authorId, Long wallOwnerId) {
        validatePostContent(content);

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        User wallOwner = userRepository.findById(wallOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("Wall owner not found"));

        // Check if the author is the wall owner or they are friends
        if (!author.getId().equals(wallOwnerId) && !author.getFriends().contains(wallOwner)) {
            throw new IllegalArgumentException("You can only post on your own wall or your friends' walls");
        }

        Post post = new Post(content, author);
        post.setWallOwner(wallOwner);
        Post savedPost = postRepository.save(post);

        // Create notification for the wall owner if different from author using NotificationService
        if (!author.getId().equals(wallOwnerId)) {
            notificationService.createNotification(
                    wallOwnerId,
                    author.getFirstName() + " " + author.getLastName() + " posted on your wall",
                    Notification.NotificationType.NEW_POST,
                    savedPost.getId()
            );
        }

        return savedPost;
    }

    @Transactional
    public Post createGroupPost(String content, Long authorId, Long groupId) {
        validatePostContent(content);

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Check if the author is a member of the group
        if (!group.getMembers().contains(author)) {
            throw new IllegalArgumentException("You must be a member of the group to post");
        }

        // Check posting permissions
        if (group.getPostPermission() == GroupPermission.ADMIN_ONLY &&
            !group.getCreator().equals(author)) {
            throw new IllegalArgumentException("Only the group admin can post in this group");
        }

        Post post = new Post(content, author);
        post.setGroup(group);
        Post savedPost = postRepository.save(post);

        // Create notifications for group members except the author using NotificationService
        for (User member : group.getMembers()) {
            if (!member.getId().equals(authorId)) {
                notificationService.createNotification(
                        member.getId(),
                        author.getFirstName() + " " + author.getLastName() + " posted in group " + group.getName(),
                        Notification.NotificationType.NEW_POST,
                        savedPost.getId()
                );
            }
        }

        return savedPost;
    }

    private void validatePostContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Post content cannot be empty");
        }

        if (content.length() > MAX_POST_LENGTH) {
            throw new IllegalArgumentException("Post content exceeds maximum length of " + MAX_POST_LENGTH + " characters");
        }
    }

    @Transactional(readOnly = true)
    public Post findById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
    }

    @Transactional(readOnly = true)
    public List<Post> findByAuthor(Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        return postRepository.findByAuthor(author);
    }

    @Transactional(readOnly = true)
    public List<Post> findByWallOwner(Long wallOwnerId) {
        User wallOwner = userRepository.findById(wallOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("Wall owner not found"));
        return postRepository.findByWallOwner(wallOwner);
    }

    @Transactional(readOnly = true)
    public List<Post> findByGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        return postRepository.findByGroup(group);
    }

    @Transactional(readOnly = true)
    public List<Post> findActivePostsByWallOwner(Long wallOwnerId) {
        return postRepository.findActivePostsByWallOwnerId(wallOwnerId);
    }

    @Transactional(readOnly = true)
    public List<Post> findActivePostsByGroup(Long groupId) {
        return postRepository.findActivePostsByGroupId(groupId);
    }

    @Transactional(readOnly = true)
    public List<Post> getFeedForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Get IDs of all friends
        List<Long> friendIds = user.getFriends().stream()
                .map(User::getId)
                .collect(Collectors.toList());
        
        // Add user's own ID to include their posts
        friendIds.add(userId);
        
        return postRepository.findFeedPostsByFriendIds(friendIds);
    }

    @Transactional
    public Post updatePost(Long postId, String content, Long authorId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // Check if the user is the author
        if (!post.getAuthor().getId().equals(authorId)) {
            throw new IllegalArgumentException("Only the author can update the post");
        }

        validatePostContent(content);

        post.setContent(content);
        post.setUpdatedAt(LocalDateTime.now());
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // Check if the user is the author, wall owner, or group admin
        boolean isAuthor = post.getAuthor().getId().equals(userId);
        boolean isWallOwner = post.getWallOwner() != null && post.getWallOwner().getId().equals(userId);
        boolean isGroupAdmin = post.getGroup() != null && post.getGroup().getCreator().getId().equals(userId);

        if (!(isAuthor || isWallOwner || isGroupAdmin)) {
            throw new IllegalArgumentException("You don't have permission to delete this post");
        }

        postRepository.delete(post);
    }

    @Transactional
    public Post markPostAsDeleted(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // Check if the user is the author, wall owner, or group admin
        boolean isAuthor = post.getAuthor().getId().equals(userId);
        boolean isWallOwner = post.getWallOwner() != null && post.getWallOwner().getId().equals(userId);
        boolean isGroupAdmin = post.getGroup() != null && post.getGroup().getCreator().getId().equals(userId);

        if (!(isAuthor || isWallOwner || isGroupAdmin)) {
            throw new IllegalArgumentException("You don't have permission to delete this post");
        }

        post.setDeleted(true);
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public long countCommentsByPostId(Long postId) {
        // Check if post exists
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("Post not found");
        }
        return postRepository.countCommentsByPostId(postId);
    }

    @Transactional(readOnly = true)
    public boolean isPostOwner(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return post.getAuthor().getId().equals(userId);
    }
}
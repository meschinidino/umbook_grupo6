package grupo6.umbook.controller;

import grupo6.umbook.model.Notification;
import grupo6.umbook.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsForUser(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotificationsForUser(@PathVariable Long userId) {
        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Long>> countUnreadNotificationsForUser(@PathVariable Long userId) {
        long count = notificationService.countUnreadNotificationsForUser(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        try {
            Notification notification = notificationService.markAsRead(notificationId, userId);
            return ResponseEntity.ok(notification);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long userId) {
        try {
            notificationService.markAllAsRead(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "All notifications marked as read");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam Long userId) {
        try {
            notificationService.deleteNotification(notificationId, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification deleted successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/create-birthday-notifications")
    public ResponseEntity<?> createBirthdayNotifications() {
        try {
            notificationService.createBirthdayNotifications();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Birthday notifications created successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(
            @PathVariable Long userId,
            @PathVariable String type) {
        try {
            Notification.NotificationType notificationType = Notification.NotificationType.valueOf(type);
            List<Notification> notifications = notificationService.getNotificationsByType(userId, notificationType);
            return ResponseEntity.ok(notifications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/user/{userId}/reference/{referenceId}")
    public ResponseEntity<List<Notification>> getNotificationsByReference(
            @PathVariable Long userId,
            @PathVariable Long referenceId) {
        List<Notification> notifications = notificationService.getNotificationsByReference(userId, referenceId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            String message = (String) request.get("message");
            String type = (String) request.get("type");
            Long referenceId = request.get("referenceId") != null ? 
                    Long.valueOf(request.get("referenceId").toString()) : null;

            if (userId == null || message == null || type == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "User ID, message, and type are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Notification.NotificationType notificationType = Notification.NotificationType.valueOf(type);
            
            Notification notification;
            if (referenceId != null) {
                notification = notificationService.createNotification(userId, message, notificationType, referenceId);
            } else {
                notification = notificationService.createNotification(userId, message, notificationType);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
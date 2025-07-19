package grupo6.umbook.controller;

import grupo6.umbook.model.FriendRequest;
import grupo6.umbook.model.User;
import grupo6.umbook.service.FriendRequestService;
import grupo6.umbook.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friend-requests")
public class FriendRequestController {

    private final FriendRequestService friendRequestService;
    private final UserService userService;

    @Autowired
    public FriendRequestController(FriendRequestService friendRequestService, UserService userService) {
        this.friendRequestService = friendRequestService;
        this.userService = userService;
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendFriendRequest(@RequestBody Map<String, Long> request) {
        try {
            Long senderId = request.get("senderId");
            Long receiverId = request.get("receiverId");

            if (senderId == null || receiverId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Sender ID and receiver ID are required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            FriendRequest friendRequest = friendRequestService.sendFriendRequest(senderId, receiverId);
            return ResponseEntity.status(HttpStatus.CREATED).body(friendRequest);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long requestId, @RequestBody Map<String, Long> request) {
        try {
            Long receiverId = request.get("receiverId");

            if (receiverId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Receiver ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            FriendRequest acceptedRequest = friendRequestService.acceptFriendRequest(requestId, receiverId);
            return ResponseEntity.ok(acceptedRequest);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable Long requestId, @RequestBody Map<String, Long> request) {
        try {
            Long receiverId = request.get("receiverId");

            if (receiverId == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Receiver ID is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            FriendRequest rejectedRequest = friendRequestService.rejectFriendRequest(requestId, receiverId);
            return ResponseEntity.ok(rejectedRequest);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/pending/{userId}")
    public ResponseEntity<List<FriendRequest>> getPendingRequests(@PathVariable Long userId) {
        List<FriendRequest> pendingRequests = friendRequestService.getPendingRequestsForUser(userId);
        return ResponseEntity.ok(pendingRequests);
    }

    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<FriendRequest>> getSentRequests(@PathVariable Long userId) {
        List<FriendRequest> sentRequests = friendRequestService.getSentRequestsByUser(userId);
        return ResponseEntity.ok(sentRequests);
    }

    @DeleteMapping("/friends/{userId}/{friendId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        try {
            friendRequestService.removeFriend(userId, friendId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Friend removed successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/friends/{userId}")
    public ResponseEntity<List<User>> getFriends(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Remove passwords from the response
            user.getFriends().forEach(friend -> friend.setPassword(null));
            
            return ResponseEntity.ok(user.getFriends().stream().toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
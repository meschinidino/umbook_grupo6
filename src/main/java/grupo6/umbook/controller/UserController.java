package grupo6.umbook.controller;

import grupo6.umbook.model.User;
import grupo6.umbook.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Email and password are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean authenticated = userService.authenticate(email, password);
        if (authenticated) {
            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Don't return the password in the response
                user.setPassword(null);
                return ResponseEntity.ok(user);
            }
        }

        Map<String, String> response = new HashMap<>();
        response.put("error", "Invalid username or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Don't return the password in the response
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } else {
            Map<String, String> response = new HashMap<>();
            response.put("error", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String term) {
        List<User> users = userService.searchByName(term);
        // Remove passwords from the response
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            // Ensure the ID in the path matches the ID in the request body
            if (!id.equals(user.getId())) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "ID in the path does not match ID in the request body");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            User updatedUser = userService.updateUser(user);
            // Don't return the password in the response
            updatedUser.setPassword(null);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id) {
        try {
            userService.disableUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User disabled successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enableUser(@PathVariable Long id) {
        try {
            userService.enableUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "User enabled successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PutMapping("/{id}/birthday-reminder")
    public ResponseEntity<?> setBirthdayReminderDays(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        try {
            Integer days = request.get("days");
            if (days == null) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Days parameter is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            userService.setBirthdayReminderDays(id, days);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Birthday reminder days set successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        // Remove passwords from the response
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }
}
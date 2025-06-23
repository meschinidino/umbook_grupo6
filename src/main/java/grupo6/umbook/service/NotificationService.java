package grupo6.umbook.service;

import grupo6.umbook.model.Notification;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.NotificationRepository;
import grupo6.umbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Notification createNotification(Long userId, String message, Notification.NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Notification notification = new Notification(user, message, type);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification createNotification(Long userId, String message, Notification.NotificationType type, Long referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Notification notification = new Notification(user, message, type, referenceId);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findUnreadByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long countUnreadNotificationsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        // Check if the notification belongs to the user
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Notification does not belong to this user");
        }

        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        // Check if the notification belongs to the user
        if (!notification.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Notification does not belong to this user");
        }

        notificationRepository.delete(notification);
    }

    @Transactional
    public void createBirthdayNotifications() {
        // Get users with birthdays today
        List<User> usersWithBirthdayToday = userRepository.findUsersWithBirthdayOn(LocalDate.now());

        // For each user with a birthday, notify their friends
        for (User birthdayUser : usersWithBirthdayToday) {
            for (User friend : birthdayUser.getFriends()) {
                // Check if friend has set up birthday reminders
                if (friend.getBirthdayReminderDays() != null && friend.getBirthdayReminderDays() >= 0) {
                    Notification notification = new Notification(
                            friend,
                            "Today is " + birthdayUser.getFirstName() + " " + birthdayUser.getLastName() + "'s birthday!",
                            Notification.NotificationType.BIRTHDAY_REMINDER,
                            birthdayUser.getId()
                    );
                    notificationRepository.save(notification);
                }
            }
        }

        // Get users with upcoming birthdays for those who have set reminders
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            // Skip users who haven't set up reminders
            if (user.getBirthdayReminderDays() == null || user.getBirthdayReminderDays() <= 0) {
                continue;
            }

            // Find friends with birthdays in the next N days
            // Get all enabled users with birthdate and filter in the service layer
            List<User> allUsersWithBirthday = userRepository.findByEnabledTrueAndBirthDateIsNotNull();
            List<User> friendsWithUpcomingBirthdays = filterUsersWithBirthdayInNextDays(
                    allUsersWithBirthday, LocalDate.now(), user.getBirthdayReminderDays());

            // Create notifications for upcoming birthdays
            for (User upcomingBirthdayFriend : friendsWithUpcomingBirthdays) {
                // Only notify if they are friends
                if (user.getFriends().contains(upcomingBirthdayFriend)) {
                    Notification notification = new Notification(
                            user,
                            upcomingBirthdayFriend.getFirstName() + " " + upcomingBirthdayFriend.getLastName() + 
                            "'s birthday is coming up soon!",
                            Notification.NotificationType.BIRTHDAY_REMINDER,
                            upcomingBirthdayFriend.getId()
                    );
                    notificationRepository.save(notification);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByType(Long userId, Notification.NotificationType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findByUserIdAndType(userId, type);
    }

    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByReference(Long userId, Long referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return notificationRepository.findByUserIdAndReferenceId(userId, referenceId);
    }

    /**
     * Filter users whose birthdays fall within the next N days from the reference date.
     * This method handles both cases where the date range crosses a month boundary
     * and where it crosses a year boundary.
     * 
     * @param users List of users to filter
     * @param referenceDate The reference date to start counting from
     * @param days Number of days to look ahead
     * @return Filtered list of users with birthdays in the next N days
     */
    private List<User> filterUsersWithBirthdayInNextDays(List<User> users, LocalDate referenceDate, int days) {
        return users.stream()
                .filter(user -> {
                    if (user.getBirthDate() == null) {
                        return false;
                    }

                    // Get month and day of birth
                    int birthMonth = user.getBirthDate().getMonthValue();
                    int birthDay = user.getBirthDate().getDayOfMonth();

                    // Check for birthdays in the next N days
                    LocalDate endDate = referenceDate.plusDays(days);

                    // If we're not crossing a year boundary
                    if (referenceDate.getMonthValue() <= endDate.getMonthValue()) {
                        // Birthday is in the same month as reference date
                        if (birthMonth == referenceDate.getMonthValue() && 
                            birthDay >= referenceDate.getDayOfMonth() && 
                            birthDay <= endDate.getDayOfMonth()) {
                            return true;
                        }

                        // Birthday is in the same month as end date (if different from reference month)
                        if (referenceDate.getMonthValue() != endDate.getMonthValue() && 
                            birthMonth == endDate.getMonthValue() && 
                            birthDay <= endDate.getDayOfMonth()) {
                            return true;
                        }

                        // Birthday is in a month between reference and end date
                        if (birthMonth > referenceDate.getMonthValue() && 
                            birthMonth < endDate.getMonthValue()) {
                            return true;
                        }
                    } else {
                        // We're crossing a year boundary (e.g., December to January)
                        // Birthday is after reference date in the current year
                        if (birthMonth == referenceDate.getMonthValue() && 
                            birthDay >= referenceDate.getDayOfMonth()) {
                            return true;
                        }

                        // Birthday is before end date in the next year
                        if (birthMonth == endDate.getMonthValue() && 
                            birthDay <= endDate.getDayOfMonth()) {
                            return true;
                        }

                        // Birthday is in a month after reference month in current year
                        if (birthMonth > referenceDate.getMonthValue()) {
                            return true;
                        }

                        // Birthday is in a month before end month in next year
                        if (birthMonth < endDate.getMonthValue()) {
                            return true;
                        }
                    }

                    return false;
                })
                .toList();
    }
}

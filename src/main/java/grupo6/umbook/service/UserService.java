package grupo6.umbook.service;

import grupo6.umbook.model.User;
import grupo6.umbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> searchByName(String searchTerm) {
        return userRepository.findByNameContaining(searchTerm);
    }

    @Transactional
    public User updateUser(User user) {
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("User not found");
        }
        return userRepository.save(user);
    }

    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void setBirthdayReminderDays(Long userId, Integer days) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setBirthdayReminderDays(days);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> findUsersWithBirthdayToday() {
        return userRepository.findUsersWithBirthdayOn(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<User> findUsersWithBirthdayInNextDays(int days) {
        List<User> usersWithBirthDate = userRepository.findByEnabledTrueAndBirthDateIsNotNull();
        return filterUsersWithBirthdayInNextDays(usersWithBirthDate, LocalDate.now(), days);
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

    @Transactional(readOnly = true)
    public boolean authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.isEnabled() && user.getPassword().equals(password);
        }
        return false;
    }
}

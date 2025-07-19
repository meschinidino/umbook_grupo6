package grupo6.umbook.service;

import grupo6.umbook.model.Notification;
import grupo6.umbook.model.User;
import grupo6.umbook.repository.NotificationRepository;
import grupo6.umbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository; // Inyecta el nuevo repositorio

    @Autowired
    public UserService(UserRepository userRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public User registerUser(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<User> findByFirstName(String firstName) {
        return userRepository.findByFirstName(firstName);
    }

    @Transactional(readOnly = true)
    public List<User> findByLastName(String lastName) {
        return userRepository.findByLastName(lastName);
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
    public List<User> findUsersWithBirthdayInNextDays(int days) {
        List<User> usersWithBirthDate = userRepository.findAllActiveUsersWithBirthDate();

        // Genera un conjunto de fechas (mes-día) para los próximos 'n' días.
        LocalDate today = LocalDate.now();
        Set<MonthDay> upcomingBirthdays = IntStream.range(0, days)
                .mapToObj(i -> today.plusDays(i))
                .map(MonthDay::from)
                .collect(Collectors.toSet());

        // Filtra los usuarios cuyo cumpleaños coincide con alguna de las fechas.
        return usersWithBirthDate.stream()
                .filter(user -> upcomingBirthdays.contains(MonthDay.from(user.getBirthDate())))
                .collect(Collectors.toList());
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

    @Transactional
    public void createBirthdayNotifications() {
        // 1. Obtiene el mes y día de hoy para comparar
        MonthDay today = MonthDay.now();

        // 2. Busca todos los usuarios activos que tengan una fecha de nacimiento registrada
        List<User> usersWithBirthDate = userRepository.findAllActiveUsersWithBirthDate();

        // 3. Filtra la lista para obtener solo los que cumplen años HOY
        List<User> birthdayUsers = usersWithBirthDate.stream()
                .filter(user -> MonthDay.from(user.getBirthDate()).equals(today))
                .toList();

        // 4. Por cada usuario que cumple años, notifica a sus amigos
        for (User birthdayUser : birthdayUsers) {
            // El @Transactional se encarga de que la carga de `getFriends()` funcione
            for (User friend : birthdayUser.getFriends()) {
                // Verifica si el amigo desea recibir recordatorios
                if (friend.getBirthdayReminderDays() != null && friend.getBirthdayReminderDays() >= 0) {

                    String message = "¡Hoy es el cumpleaños de " + birthdayUser.getFirstName() + " " + birthdayUser.getLastName() + "! 🎂";

                    Notification notification = new Notification(
                            friend, // El amigo RECIBE la notificación
                            message,
                            Notification.NotificationType.BIRTHDAY_REMINDER,
                            birthdayUser.getId() // Guardamos el ID de quien cumple años
                    );
                    notificationRepository.save(notification);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.isEnabled() && user.getPassword().equals(password);
        }
        return false;
    }
}

package grupo6.umbook.config;

import grupo6.umbook.model.User;
import grupo6.umbook.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User zada = new User("Zada", "Jackson", LocalDate.of(1995, 5, 10), "F", "zada@example.com", "123456", "123");
                User craig = new User("Craig", "Saris", LocalDate.of(1990, 3, 22), "M", "craig@example.com", "654321", "123");
                userRepository.save(zada);
                userRepository.save(craig);
            }
        };
    }
}
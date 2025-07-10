# umbook_grupo6

## Database Configuration

For a detailed explanation of how the database is created and set up in this application, see [Database Setup Explanation](src/main/resources/db_setup_explanation.md).

### Current Configuration: H2 In-Memory Database

The application is currently configured to use an H2 in-memory database for development and testing purposes. This allows the application to run without requiring an external MySQL database.

### H2 Console

You can access the H2 console at: http://localhost:8080/h2-console

Connection details:
- JDBC URL: `jdbc:h2:mem:umbook`
- Username: `sa`
- Password: (empty)

### Switching to MySQL

If you want to use MySQL instead of H2, follow these steps:

1. Ensure MySQL is installed and running on your machine
2. Create a database named `umbook` (or update the configuration to use a different database name)
3. Update `application.properties` to use MySQL:

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/umbook?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect

# Comment out or remove the H2 configuration
# spring.datasource.url=jdbc:h2:mem:umbook;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
# spring.datasource.username=sa
# spring.datasource.password=
# spring.datasource.driver-class-name=org.h2.Driver
# spring.h2.console.enabled=true
# spring.h2.console.path=/h2-console
```

## Troubleshooting

### MySQL Connection Issues

If you encounter MySQL connection issues like:

```
Communications link failure
Connection refused: getsockopt
```

Check the following:

1. Ensure MySQL is running on your machine
2. Verify the MySQL port (default is 3306)
3. Check that the username and password in `application.properties` match your MySQL configuration
4. Make sure the database exists or `createDatabaseIfNotExist=true` is set in the connection URL

## Recent Changes

### Birthday Notification Feature

The application includes a feature to notify users about their friends' birthdays. This feature works as follows:

1. Users can set a reminder for birthdays by specifying how many days in advance they want to be notified
2. The system checks for upcoming birthdays and creates notifications for users

#### Implementation Details

The birthday notification feature was recently updated to improve database compatibility:

- The complex JPQL query in `UserRepository.findUsersWithBirthdayInNextDays` was replaced with a simpler approach
- Birthday filtering now happens in the service layer instead of at the database level
- This change ensures compatibility with both H2 and MySQL databases
- The implementation handles edge cases like month boundaries and year boundaries

This approach provides better maintainability and database portability while preserving the same functionality.

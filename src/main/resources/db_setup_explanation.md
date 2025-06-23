# Database Setup and Configuration in UMBook Application

## Overview
This document explains how the database is created and set up in the UMBook application. The application uses Spring Boot with Spring Data JPA and Hibernate for database operations.

## Database Configuration

### Configuration Files
The primary configuration for the database is in `src/main/resources/application.properties`. Currently, the application is configured to use an H2 in-memory database for development and testing, but it can be switched to MySQL by uncommenting the MySQL configuration.

```properties
# H2 In-Memory Database Configuration (for development/testing)
spring.datasource.url=jdbc:h2:mem:umbook;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate Properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

### Dependencies
The database-related dependencies are defined in `build.gradle`:

```gradle
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.mysql:mysql-connector-j'
    runtimeOnly 'com.h2database:h2'
    // other dependencies...
}
```

## How the Database is Created

### Automatic Schema Generation
Spring Boot, through Hibernate, automatically creates the database schema based on the JPA entity classes. The key property that controls this behavior is:

```properties
spring.jpa.hibernate.ddl-auto=update
```

This property tells Hibernate to:
- Create tables if they don't exist
- Update existing tables to match the entity definitions
- Preserve existing data

### Entity Classes
The database structure is defined by JPA entity classes in the `grupo6.umbook.model` package. Each entity class corresponds to a table in the database. For example, the `User` class:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;
    
    // other fields, relationships, and methods...
}
```

JPA annotations in these classes define:
- Tables and their names (`@Entity`, `@Table`)
- Primary keys (`@Id`, `@GeneratedValue`)
- Columns and their constraints (`@Column`)
- Relationships between tables (`@OneToMany`, `@ManyToOne`, `@ManyToMany`, etc.)

### Database Initialization Process

When the application starts:

1. Spring Boot reads the database configuration from `application.properties`
2. It sets up a connection pool using the configured datasource
3. Hibernate scans all classes annotated with `@Entity`
4. Based on the `spring.jpa.hibernate.ddl-auto` setting, Hibernate generates and executes the necessary DDL (Data Definition Language) statements to create or update the database schema
5. Spring Data JPA creates implementations of repository interfaces at runtime

## Repository Interfaces

Spring Data JPA repository interfaces provide methods for database operations without requiring explicit SQL queries. For example, the `UserRepository`:

```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByNameContaining(@Param("searchTerm") String searchTerm);
    
    // other methods...
}
```

These interfaces:
- Extend `JpaRepository` to inherit standard CRUD operations
- Define custom query methods using method naming conventions
- Use `@Query` annotations for more complex queries

## Switching Between Databases

The application can switch between H2 and MySQL by changing the configuration in `application.properties`. This flexibility allows for:
- Using H2 in-memory database for development and testing
- Using MySQL for production

## Conclusion

The database in the UMBook application is created and set up automatically by Spring Boot and Hibernate based on:
1. The configuration in `application.properties`
2. The JPA entity classes that define the database structure
3. The Spring Data JPA repositories that provide database operations

This approach eliminates the need for manual database schema creation and allows the application to work with different database systems with minimal configuration changes.
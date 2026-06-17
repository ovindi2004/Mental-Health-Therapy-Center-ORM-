# Mental Health Therapy Center

<<<<<<< HEAD
## Screenshot

![Home Page](screenshots/img.png)
=======
A desktop management system for a mental health therapy center, built with **JavaFX** and **Hibernate (JPA)**. It supports patient management, therapist management, therapy program management, session scheduling, payments, and reporting, with role-based access for **Admin** and **Receptionist** users.

## Features

- **Authentication** — secure login with BCrypt-hashed passwords and role selection (Admin / Receptionist)
- **Patient Management** — register and maintain patient records, including therapy progress
- **Therapist Management** — manage therapist profiles, specializations, and availability
- **Therapy Program Management** — define programs, durations, fees, and assigned therapists
- **Session Scheduling** — schedule and track therapy sessions between patients and therapists
- **Payments** — record and track patient payments and payment status
- **Reports** — generate operational reports
- **Role-based Dashboards** — separate dashboards for Admin and Receptionist
- **Change Password** — allow logged-in users to update their credentials

## Tech Stack

- **Java 21**
- **JavaFX 21** (UI, FXML views)
- **Hibernate ORM 6.5** (JPA persistence)
- **MySQL** (primary database, via `mysql-connector-j`)
- **H2** (available as an alternative/embedded database)
- **Ehcache** + **JCache** (Hibernate second-level and query caching)
- **jBCrypt** / **Spring Security Crypto** (password hashing)
- **Lombok**
- **Maven** (build tool, with the `javafx-maven-plugin`)
- **JUnit 5** (testing)

## Architecture

The project follows a layered architecture:

```
Controller (JavaFX/FXML)  →  BO (Business Object)  →  DAO (Data Access Object)  →  Entity (Hibernate)
```

- `entity` — JPA-annotated domain classes (`Patient`, `Therapist`, `TherapyProgram`, `TherapySession`, `Payment`, `User`)
- `dto` — Data Transfer Objects used between the UI and business layer
- `dao` / `dao.custom` / `dao.custom.impl` — data access interfaces and Hibernate-based implementations, created via `DAOFactory`
- `bo` / `bo.custom` / `bo.custom.impl` — business logic interfaces and implementations, created via `BOFactory`
- `controller` — JavaFX controllers wired to FXML views in `src/main/resources`
- `Config/FactoryConfiguration` — builds the Hibernate `SessionFactory` and seeds default users on first run
- `Launcher` — JavaFX application entry point

## Prerequisites

- JDK 21+
- Maven 3.9+ (or use the bundled `mvnw` / `mvnw.cmd` wrapper)
- MySQL Server running locally (or update the connection settings to point elsewhere)

## Database Setup

The application uses Hibernate's `hbm2ddl.auto=update`, so tables are created/updated automatically — you only need an empty database to exist (or let the app create it).

Connection settings are in `src/main/resources/hibernate.properties`:

```properties
hibernate.connection.url=jdbc:mysql://localhost:3306/mental_TDb?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
hibernate.connection.username=root
hibernate.connection.password=mysql
```

Update the username/password to match your local MySQL setup before running the app. With `createDatabaseIfNotExist=true`, the `mental_TDb` schema will be created automatically if it doesn't exist.

## Running the Application

Using the Maven wrapper:

```bash
# Linux / macOS
./mvnw clean javafx:run

# Windows
mvnw.cmd clean javafx:run
```

Or, if you have Maven installed globally:

```bash
mvn clean javafx:run
```

## Default Login Credentials

On first run, the application automatically seeds two default users (passwords are hashed with BCrypt):

| Role         | Username       | Password   |
|--------------|----------------|------------|
| Admin        | `admin`        | `admin123` |
| Receptionist | `receptionist` | `recep123` |

It's recommended to change these default passwords after the first login.

## Project Structure

```
src/main/java/lk/ijse/mental_health_therapy/
├── Config/                 # Hibernate SessionFactory setup & user seeding
├── bo/                     # Business Object interfaces + custom/impl
├── controller/             # JavaFX FXML controllers
├── dao/                    # DAO interfaces + custom/impl
├── dto/                    # Data Transfer Objects
├── entity/                 # JPA entities
└── Launcher.java           # Application entry point

src/main/resources/
├── *.fxml                  # JavaFX views (login, dashboards, management screens)
├── asses/Style/*.css       # Stylesheets per screen
├── hibernate.properties    # Database & Hibernate configuration
└── ehcache.xml             # Second-level cache configuration
```

## Notes

- This is an academic/portfolio project (package namespace `lk.ijse`), built as part of a Java/JavaFX coursework project.
- The database name, credentials, and cache settings in `hibernate.properties` are intended for local development and should be changed for any production-like use.
>>>>>>> 814f40934fd2d88975107806535dca1671c93502
